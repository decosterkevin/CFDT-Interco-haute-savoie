package decoster.cfdt.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import decoster.cfdt.AppConfig;
import decoster.cfdt.R;
import decoster.cfdt.helper.AddressParser;
import decoster.cfdt.helper.LineParser;
import decoster.cfdt.helper.SQLiteHandler;
import decoster.cfdt.helper.SessionManager;
import decoster.cfdt.helper.TableBuilder;
import jxl.Cell;
import jxl.Sheet;

public class MainActivity extends AppCompatActivity {
    public static SQLiteHandler db;
    public static SessionManager session;
    public static ArrayList<TableBuilder> tbs;
    private final int maxStringLenght = 30;
    private final String DIRNAME = "xls_files";
    ProgressDialog dialog;
    private LinearLayout sv;
    private LinearLayout pathPanel;
    private Context context = null;
    private TextView info;
    private FloatingActionButton fab;
    private TableBuilder currentTb = null;
    private Sheet currentSheet = null;
    private int currentRow = -1;
    private ArrayList<EditText> editTexts = new ArrayList<>();
    private HashSet<Integer> modifiedEditText = new HashSet<>();
    private Drawable valideImage;
    private HashMap<String, String> userDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        valideImage = ResourcesCompat.getDrawable(getResources(), R.drawable.valid, null);

        db = new SQLiteHandler(getApplicationContext());

        context = this;
        // Progress dialog
        // session manager


        try {
            session = new SessionManager(getApplicationContext());
            if (!session.isLoggedIn()) {
                registerUser();
            }
            userDetails = db.getUserDetails();
        } catch (Exception e) {
            e.printStackTrace();
            logoutUser();
        }
        //Build tableBuilder for every files in AppConfig (can be improve)


        dialog = new ProgressDialog(MainActivity.this);

        sv = (LinearLayout) findViewById(R.id.ScrollPanel);
        pathPanel = (LinearLayout) findViewById(R.id.pathPanel);
        pathPanel.setVisibility(LinearLayout.GONE);
        info = (TextView) findViewById(R.id.info);
        info.setClickable(false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendXLS();
            }
        });
        fab.hide();

        new LoadingTableTask().execute(new Boolean[]{new Boolean(false)});

    }

    private void sendXLS() {
        final Sheet specSheet = currentSheet;

        final Dialog diag = new Dialog(this, R.style.PauseDialog);
        diag.setContentView(R.layout.custom_send_layout);
        diag.setTitle("envoie du fichier");
        TextView text = (TextView) diag.findViewById(R.id.text);
        String previousText = (String) text.getText();
        text.setText(previousText + " " + currentTb.getName() + " à :");
        final EditText editText = (EditText) diag.findViewById(R.id.editText);
        editText.setHint(userDetails.get("manager_email"));
        diag.show();

        Button yes = (Button) diag.findViewById(R.id.dialogButtonYes);
        Button no = (Button) diag.findViewById(R.id.dialogButtonNo);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                diag.dismiss();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                diag.dismiss();
                String surname = userDetails.get("user_surname");
                String name = userDetails.get("user_name");
                String email = userDetails.get("user_email");
                String fileName = currentTb.getName();
                //String  filename = currentTb.createNewFileFromSheet(specSheet);

                String filepath = currentTb.getPath();
                final String sender = editText.getText().toString();
                if (filepath != null) {
                    String url = AppConfig.SERVER_URL + "/api/" + userDetails.get("access_code");
                    SimpleMultiPartRequest smr = new SimpleMultiPartRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d(MainActivity.class.getSimpleName(), response);
                                    Toast.makeText(getApplicationContext(), "succès", Toast.LENGTH_LONG).show();
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                    smr.addStringParam("surname", surname);
                    smr.addStringParam("name", name);
                    smr.addStringParam("email", email);
                    smr.addFile("file", filepath);

                    RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
                    mRequestQueue.add(smr);
                    Log.d(MainActivity.class.getSimpleName(), "EMAIL SEND WITH SUCCESS");

                }
            }
        });


    }

    //List all files containings in AppConfig for selection
    private void buildFilesButtons() {

        sv.removeAllViews();
        info.setText("");
        info.setClickable(false);
        fab.hide();
        pathPanel.setVisibility(LinearLayout.GONE);
        setState(null, null, -1);
        for (final TableBuilder tb : tbs) {
            Button myButton = new Button(this);
            myButton.setText(tb.getName());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            sv.addView(myButton, lp);
            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buildSectionsButtons(tb);
                }
            });
        }

    }

    //Once the file is selected, list all sheets containing in that file (ie: The section)
    private void buildSectionsButtons(final TableBuilder tb) {
        sv.removeAllViews();
        setState(tb, null, -1);

        fab.show();
        pathPanel.setVisibility(LinearLayout.VISIBLE);

        info.setText(tb.getName());
        info.setClickable(false);
        for (final Sheet sh : tb.getSheets()) {

            Button myButton = new Button(this);
            myButton.setText(sh.getName());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            sv.addView(myButton, lp);

            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buildCollectivitiesButtons(tb, sh);
                }
            });
            if (currentTb.isSheetComplete(sh)) {
                myButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, valideImage, null);
            }
        }

    }

    //Once the section (ie: SHeet) is selected, list all possible row entries (ie: the collectivity)
    private void buildCollectivitiesButtons(final TableBuilder tb, final Sheet sh) {
        sv.removeAllViews();
        setState(tb, sh, -1);
        info.setText(sh.getName());
        info.setClickable(false);
        info.setBackground(null);
        fab.show();
        int initial_row = sh.getCell(0, 0).getContents().toLowerCase().contains("COLLECTIVITE".toLowerCase()) ? 1 : 2;
        for (int r = initial_row; r < sh.getRows(); r++) {
            final int row = r;
            String textCell = sh.getCell(0, r).getContents();
            if (!textCell.equals("")) {

                Button myButton = new Button(this);
                textCell = textCell.trim().replaceAll(" +", " ");
                textCell = (textCell.length() > maxStringLenght) ? textCell.substring(0, maxStringLenght) + "..." : textCell;
                myButton.setText(textCell);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                sv.addView(myButton, lp);

                myButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        buildEditableCollecButtons(tb, sh, row);
                    }
                });
                if (currentTb.isRowComplete(sh, row)) {

                    myButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, valideImage, null);
                }
            }

        }
    }

    //Once the collectivity is selected, list all entries to be edited.
    private void buildEditableCollecButtons(final TableBuilder tb, final Sheet sh, final int row) {
        sv.removeAllViews();
        fab.show();
        info.setText(sh.getCell(0, row).getContents());
        info.setClickable(true);
        info.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.textbutton, null));
        setState(tb, sh, row);


        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] adress = AddressParser.parseText(sh.getCell(0, row).getContents());
                if (Arrays.asList(adress).contains("")) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.adressnotfound)
                            , Toast.LENGTH_LONG)
                            .show();
                } else {
                    //Uri gmmIntentUri = Uri.parse("google.navigation:q=Taronga+Zoo,+Sydney+Australia");
                    new AlertDialog.Builder(MainActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.navigation)
                            .setMessage(getResources().getString(R.string.really_navigation) + " " + adress[0] + ", " + adress[1] + " " + adress[2] + "?")
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + adress[0] + ",+" + adress[1] + "+" + adress[2]);
                                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                    mapIntent.setPackage("com.google.android.apps.maps");
                                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                                        startActivity(mapIntent);
                                    }

                                }

                            })
                            .setNegativeButton(R.string.no, null)
                            .show();


                }


            }
        });
        int initial_row = sh.getCell(0, 0).getContents().toLowerCase().contains("COLLECTIVITE".toLowerCase()) ? 1 : 2;
        Cell[] labels = sh.getRow(initial_row - 1);
        editTexts = new ArrayList<>();
        modifiedEditText = new HashSet<>();
        for (int c = 1; c < labels.length; c++) {
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams txp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams exp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            ll.setLayoutParams(lp);
            TextView tx = new TextView(this);
            tx.setLayoutParams(txp);
            tx.setText(labels[c].getContents() + " :");
            final int col = c;
            final EditText ex = new EditText(this);
            ex.setLayoutParams(exp);
            ex.setText(sh.getCell(c, row).getContents());
            editTexts.add(ex);
           /* ex.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    EditText text = (EditText) v;
                    String beforeText = null;
                    if (hasFocus) {
                        beforeText = text.getText().toString();
                    } else {
                        String afterText = text.getText().toString();
                            tb.update_cell(sh, col, row, ex.getText().toString());
                            currentSheet = tb.getSheet(sheetName);
                            Log.d(MainActivity.class.getSimpleName(), currentSheet.getName());

                        }


                }
            });*/

            ex.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                    modifiedEditText.add(col - 1);
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
            ll.addView(tx);
            ll.addView(ex);
            sv.addView(ll);
        }
    }


    private void setState(TableBuilder cTb, Sheet csh, int crow) {
        this.currentRow = crow;
        this.currentSheet = csh;
        this.currentTb = cTb;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_synch) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.synchronize)
                    .setMessage(R.string.really_synch)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            new LoadingTableTask().execute(new Boolean[]{new Boolean(true)});
                        }

                    })
                    .setNegativeButton(R.string.no, null)
                    .show();


        } else if (id == R.id.action_logout) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.logout)
                    .setMessage(R.string.really_logout)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logoutUser();
                        }

                    })
                    .setNegativeButton(R.string.no, null)
                    .show();

        }

        return super.onOptionsItemSelected(item);
    }


    private void registerUser() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();
        registerUser();
    }

    @Override
    public void onBackPressed() {

        if (currentTb != null && currentSheet == null) {
            buildFilesButtons();

        } else if (currentTb != null && currentSheet != null && currentRow == -1) {
            buildSectionsButtons(currentTb);
        } else if (currentTb != null && currentSheet != null && currentRow != -1) {

            new SavingChangeTask().execute();

        } else {

        }
    }

    private class LoadingTableTask extends AsyncTask<Boolean, Void, ArrayList<TableBuilder>> {

        @Override
        protected ArrayList<TableBuilder> doInBackground(Boolean... params) {
            if (tbs != null) {
                for (TableBuilder tb : tbs) {
                    tb.closeFile();
                }
            }
            tbs = new ArrayList<TableBuilder>();
            URL urlFiles = null;

            File path = new File(context.getFilesDir(), DIRNAME);
            if (!path.exists()) {
                path.mkdir();
            }
            File listFiles = new File(path, "filesToDownload.txt");
            if (listFiles.length() == 0 || params[0]) {
                Log.d(MainActivity.class.getSimpleName(), "list files not found, creation... ");
                try {
                    Log.d(MainActivity.class.getSimpleName(), userDetails.get("gdrive_url"));
                    urlFiles = new URL(userDetails.get("gdrive_url"));
                    FileUtils.copyURLToFile(urlFiles, listFiles, 100000, 100000);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (listFiles.length() != 0) {
                ArrayList<String[]> files = (ArrayList<String[]>) LineParser.parse(listFiles);

                for (int i = 0; i < files.size(); i++) {
                    String[] entry = files.get(i);
                    Log.d("TAG1", entry[0] + " " + entry[1]);

                    try {
                        String filename;
                        URL url = new URL("https://drive.google.com/uc?export=download&id=" + entry[0]);
                        if (entry[1] == null) {

                            // open the connection
                            Log.d(MainActivity.class.getSimpleName(), "not filename in listFile, synch...");

                            URLConnection con = url.openConnection();
                            // get and verify the header field
                            String fieldValue = con.getHeaderField("Content-Disposition");
                            if (fieldValue == null || !fieldValue.contains("filename=\"")) {
                                // no file name there -> throw exception ...
                            }
                            // parse the file name from the header field
                            filename = fieldValue.substring(fieldValue.indexOf("filename=\"") + 10, fieldValue.indexOf(";filename*") - 1);


                            filename = filename.replaceAll(" ", "_").toLowerCase();
                            files.get(i)[1] = filename;
                            Log.d("TAG2", filename);
                        } else {
                            Log.d(MainActivity.class.getSimpleName(), "fileName in listfile");

                            filename = entry[1];
                        }
                        File newFile = new File(path, filename);

                        if (newFile.length() == 0 || params[0]) {
                            Log.d(MainActivity.class.getSimpleName(), "synch of " + newFile.getAbsolutePath());

                            FileUtils.copyURLToFile(url, newFile, 100000, 100000);
                        }

                        TableBuilder tb = new TableBuilder(newFile, path);
                        tb.createWB();
                        if (tb.getWb() != null) {
                            tbs.add(tb);
                        }


                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                LineParser.put(files, listFiles);

            }


            return tbs;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Chargement... ");
            dialog.setCancelable(false);
            if (!dialog.isShowing()) {
                dialog.show();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<TableBuilder> result) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            buildFilesButtons();

        }
    }

    private class SavingChangeTask extends AsyncTask<Void, Void, Void> {


        HashMap<Integer, String> modifiedText = new HashMap<Integer, String>();

        @Override
        protected void onPreExecute() {
            //set message of the dialog
            dialog.setMessage("Chargement... ");
            dialog.setCancelable(false);
            //show dialog

            if (!dialog.isShowing()) {
                dialog.show();
            }
            for (int editTex : modifiedEditText) {
                modifiedText.put(editTex, editTexts.get(editTex).getText().toString());
            }
            super.onPreExecute();
        }

        protected Void doInBackground(Void... args) {

            for (Map.Entry<Integer, String> entry : modifiedText.entrySet()) {
                currentTb.update_cell(currentSheet, entry.getKey() + 1, currentRow, entry.getValue());
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            // do UI work here

            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            currentSheet = currentTb.getSheet(currentSheet.getName());
            buildCollectivitiesButtons(currentTb, currentSheet);

        }
    }


}

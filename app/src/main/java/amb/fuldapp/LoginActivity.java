package amb.fuldapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Array;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import amb.fuldapp.R;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class LoginActivity extends Activity {


    
    ProgressDialog progressDialog;

    public static final String MyPREFERENCES = "MyPrefs" ;
    EditText username, passwort;
    String uservalue, passvalue, asi;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        Button loginButton = (Button) findViewById(R.id.login_button);
        username = (EditText) findViewById(R.id.username_input);
        passwort = (EditText) findViewById(R.id.passwort_input);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


        //Onclick-Listener für Login-Button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Daten werden von den Input-Feldern geholt, zu Strings gemacht und Leerzeichen entfernt
                uservalue = username.getText().toString().trim();
                passvalue = passwort.getText().toString().trim();

                //Neues Async-Task Objekt wird erzeugt
                //Nutzername und Passwort werden übergeben für Verbindung zu Qispos
                Qispos_Login qis = new Qispos_Login();
                qis.execute(uservalue, passvalue);

            }
        });

    }

    private class Qispos_Login extends AsyncTask<String, Void, Void> {

        String title,asi_href;
        String login_url = "https://qispos.hs-fulda.de/qisserver/rds?state=user&type=1&category=auth.login&startpage=portal.vm&breadCrumbSource=portal";
        String referer = "https://qispos.hs-fulda.de/qisserver/rds?state=user&type=0";


        //Loading Dialog während im Hintergrund die Connection ausgeführt wird
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setTitle("Anmelden");
            progressDialog.setMessage("Loading");
            progressDialog.setIndeterminate(false);
            progressDialog.show();
        }


        @Override
        protected Void doInBackground(String... params) {

            //Parameter der vorher erfolgten Benutzereingabe
            String username = params[0];
            String password = params[1];

            try{

                //TODO Fehlermanagement vervollständigen
                //TODO Bedingung für nicht erfolgreiche Anmeldung hinzufügen
                //TODO Bei erfolgreicher Anmeldung zur nächsten Activity wechseln
                //TODO Cookiemanagement zur Erhaltung der Session hinzufügen (JSESSIONID)

                //DOM-Objekt erzeugen nach Anfrage in Qispos mit Credentials
                Document docs = Jsoup.connect(login_url)
                        .data("asdf", username,"fdsa",password,"submit", "Anmelden")
                        .referrer(referer)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36")
                        .post();


                Element f = docs.select("div[class=divloginstatus]").first();
                title = f.text();

                //QISPOS DOM-Traversing und Regex-Extrahierung für asi-code
                //TODO Regex für ASI noch weiter spezifiern um mögliche Fehler zu verhindern
                Element e = docs.select("a[class=auflistung]").get(3);
                asi_href = e.attr("href");

                Pattern p = Pattern.compile("(?<=asi=).*");
                Matcher m = p.matcher(asi_href);
                if(m.find())
                    asi = m.group(0);


                //Shared Preferences für Session Management ASI-Code und Benutzerkennung
                SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putString("asi", asi);
                editor.putString("username", username);
                editor.putString("password", password);
                editor.commit();

            }catch(IOException e){
                e.printStackTrace();
            }

            return null;
        }

        //Setzt den greeting Text
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            TextView txtTitle = (TextView) findViewById(R.id.greeting_text);
            txtTitle.setText(title);
            progressDialog.dismiss();
        }
    }






}

package amb.fuldapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    String  asi,cookie;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        asi = sharedPreferences.getString("asi", "") ;
        cookie = sharedPreferences.getString("cookie", "") ;


        Toast.makeText(MainActivity.this, "Hallo",
                Toast.LENGTH_LONG).show();

        //FLOATING BUTTON UNTEN RECHTS
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Hallo", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //NOTEN REQUEST BUTTON
        Button b = (Button) findViewById(R.id.button);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Noten_Request req = new Noten_Request();
                req.execute(asi,cookie);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private class Noten_Request extends AsyncTask<String, String, Void> {

        String title;

        Boolean result = false;

        //Loading Dialog während im Hintergrund die Connection ausgeführt wird
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Noten abrufen");
            progressDialog.setMessage("Loading");
            progressDialog.setIndeterminate(false);
            progressDialog.show();
        }


        @Override
        protected Void doInBackground(String... params) {

            String asi = params[0];
            String cook = params[1];

            String req_url = "https://qispos.hs-fulda.de/qisserver/rds?state=notenspiegelStudent&next=list.vm&nextdir=qispos/notenspiegel/student&createInfos=Y&struct=auswahlBaum&nodeID=auswahlBaum|abschluss:abschl=84,stgnr=1&expand=0&asi="+asi+"#auswahlBaum|abschluss:abschl=84,stgnr=1";
            String referer = "https://qispos.hs-fulda.de/qisserver/rds?state=notenspiegelStudent&next=tree.vm&nextdir=qispos/notenspiegel/student&navigationPosition=functions%2CnotenspiegelStudent&breadcrumb=notenspiegel&topitem=functions&subitem=notenspiegelStudent&asi="+asi;

            String req_url2 = "https://qispos.hs-fulda.de/qisserver/rds;jsessionid="+cook+"?state=notenspiegelStudent&next=list.vm&nextdir=qispos/notenspiegel/student&createInfos=Y&struct=auswahlBaum&nodeID=auswahlBaum%7Cabschluss%3Aabschl%3D84%2Cstgnr%3D1&expand=0&asi="+asi+"#auswahlBaum%7Cabschluss%3Aabschl%3D84%2Cstgnr%3D1";
            String ref2 = "Referer: https://qispos.hs-fulda.de/qisserver/rds;jsessionid="+cook+"?state=notenspiegelStudent&next=tree.vm&nextdir=qispos/notenspiegel/student&navigationPosition=functions%2CnotenspiegelStudent&breadcrumb=notenspiegel&topitem=functions&subitem=notenspiegelStudent&asi=7JlSg5xQXwHq1SScSbzL\n";
            try{


                //DOM-Objekt erzeugen nach Anfrage der QISPOS Noten
                Connection.Response res = Jsoup.connect(req_url)
                        .referrer(referer)
                        .cookie("JSESSIONID",cook)
                        .header("Host","qispos.hs-fulda.de")
                        .header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Accept-Language","de,en-US;q=0.7,en;q=0.3")
                        .header("Accept-Encoding","")
                        .header("Connection","keep-alive")
                        .header("Upgrade-Insecure-Requests", "1")
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36")
                        .method(Connection.Method.GET)
                        .execute();

                Document docs = res.parse();

                Element notentable = docs.select("table[border=0]").get(1);

                Elements noten_row = notentable.select("tr");



                for(int i=3;i<noten_row.size()-1;i++){

                    Element eintrag = noten_row.get(i);

                    Elements spalten = eintrag.select("td");

                    title += spalten.text()+"\n";


                }






            }catch(IOException e){
                e.printStackTrace();
            }

            return null;
        }

        //Setzt den Noten Text
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            TextView t = (TextView) findViewById(R.id.textView3);
            t.setText(title);
            progressDialog.dismiss();
        }
    }

}

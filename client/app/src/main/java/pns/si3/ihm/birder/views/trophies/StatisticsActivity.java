package pns.si3.ihm.birder.views.trophies;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.enumerations.TrophyEnum;
import pns.si3.ihm.birder.models.Trophy;
import pns.si3.ihm.birder.viewmodels.UserViewModel;
import pns.si3.ihm.birder.views.account.AccountActivity;

public class StatisticsActivity extends AppCompatActivity {

    /**
     * The TAG of the activity.
     */
    private final String TAG = "Statistics";


    /**
     * The user view model.
     */
    private UserViewModel userViewModel;


    /**
     * The fields of the activity.
     */
    private TextView numberBirdShared;
    private TextView gradeText;
    private Button buttonRetour;
    private ArrayList<String> gradeList;
    ArrayList<Trophy> listTrophies = new ArrayList<>();
    ArrayList<Trophy> listTrophiesBloques = new ArrayList<>();
    ListView listTrophiesView;
    ListView listTrophiesBloquesView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        initViewModels();
        initFields();
        updateData();

    }

    private void initFields(){
        numberBirdShared = findViewById(R.id.textStatNombre);
        gradeText = findViewById(R.id.textStatGrade);
        listTrophiesView = (ListView) findViewById(R.id.listViewTrophiesDebloques);
        listTrophiesBloquesView = (ListView) findViewById(R.id.listViewTrophiesbloques);
        buttonRetour = (Button) findViewById(R.id.buttonStatRetour);
        gradeList = new ArrayList<>();


        listTrophiesView.setOnItemClickListener((parent, view, position, id) -> {
            Trophy trophy = listTrophies.get(position);
            Intent intent = new Intent(this, TrophyActivity.class);
            intent.putExtra("trophy", trophy);
            startActivity(intent);
        });

        listTrophiesBloquesView.setOnItemClickListener((parent, view, position, id) -> {
            Trophy trophy = listTrophiesBloques.get(position);
            Intent intent2 = new Intent(this, TrophyBloqueActivity.class);
            intent2.putExtra("trophy", trophy);
            startActivity(intent2);
        });


        buttonRetour.setOnClickListener(v -> goBack());

        gradeList.add("Débutant");
        gradeList.add("Intermédiaire");
        gradeList.add("Expert");
    }

    /**
     * Initializes the view models that hold the data.
     */
    private void initViewModels() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    /**
     * Update the view of the activity with the data user.
     */
    private void updateData(){
        Trophy trophy1 = new Trophy("1PhotoTrophe", TrophyEnum.UNEPHOTO);
        Trophy trophy2 = new Trophy("5PhotosTrophe", TrophyEnum.CINQPHOTOS);
        Trophy trophy3 = new Trophy("50PhotosTrophe", TrophyEnum.CINQUANTEPHOTOS);
        Trophy trophy4 = new Trophy("100PhotosTrophe", TrophyEnum.CENTPHOTOS);
        userViewModel.getUser(userViewModel.getAuthenticationId())
                .observe(this,
                        user -> {
                    if(user.isSuccessful()){
                        // Set grade
                        int numberOfReport = user.getData().getIdOfReports().size();
                        if (numberOfReport >= 10) gradeText.setText("Grade = " + gradeList.get(2));
                        else if (numberOfReport >= 5) gradeText.setText("Grade = " + gradeList.get(1));
                        else gradeText.setText("Grade = " + gradeList.get(0));

                        // Set number of bird shared
                        numberBirdShared.setText("Nombre d'oiseaux renseigné = "+ user.getData().getNumberOfBirdShared());

                        if(user.getData().getNumberPictureShared() >= 100) {
                            listTrophies.add(trophy1);
                            listTrophies.add(trophy2);
                            listTrophies.add(trophy3);
                            listTrophies.add(trophy4);
                        }
                        else if(user.getData().getNumberPictureShared() >= 50) {
                            listTrophies.add(trophy1);
                            listTrophies.add(trophy2);
                            listTrophies.add(trophy3);
                            listTrophiesBloques.add(trophy4);

                        }
                        else if(user.getData().getNumberPictureShared() >= 5) {
                            listTrophies.add(trophy1);
                            listTrophies.add(trophy2);
                            listTrophiesBloques.add(trophy3);
                            listTrophiesBloques.add(trophy4);
                        }
                        else if (user.getData().getNumberPictureShared() >= 1) {
                            listTrophies.add(trophy1);
                            listTrophiesBloques.add(trophy2);
                            listTrophiesBloques.add(trophy3);
                            listTrophiesBloques.add(trophy4);
                        }
                        else {
                            listTrophiesBloques.add(trophy1);
                            listTrophiesBloques.add(trophy2);
                            listTrophiesBloques.add(trophy3);
                            listTrophiesBloques.add(trophy4);
                        }
                        ArrayList listNoms = new ArrayList();
                        for(Trophy trophy : listTrophies){
                            String nom = trophy.getTrophyEnum().getName();
                            listNoms.add(nom);
                        }
                        ArrayList listNomsBloque = new ArrayList();
                        for(Trophy trophy : listTrophiesBloques){
                            String nom = trophy.getTrophyEnum().getName();
                            listNomsBloque.add(nom);
                        }


                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(StatisticsActivity.this, android.R.layout.simple_list_item_1, listNoms);
                        listTrophiesView.setAdapter(adapter);

                        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(StatisticsActivity.this, android.R.layout.simple_list_item_1, listNomsBloque);
                        listTrophiesBloquesView.setAdapter(adapter2);
                        Log.i(TAG,"User nb photo = " + user.getData().getNumberPictureShared());
                        Log.i(TAG,"User nb oiseaux = " + user.getData().getNumberOfBirdShared());
                    }
                        });

    }


    public void goBack() {
        Intent intent = new Intent(this, AccountActivity.class);
        startActivity(intent);
    }
}

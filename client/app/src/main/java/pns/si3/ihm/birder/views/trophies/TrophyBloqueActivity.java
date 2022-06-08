package pns.si3.ihm.birder.views.trophies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.models.Trophy;

public class TrophyBloqueActivity extends AppCompatActivity {
    Button buttonRetour;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trophy_bloque);
        Intent intent = getIntent();
        Trophy trophy = intent.getParcelableExtra("trophy");
        TextView nameTrophy = (TextView) findViewById(R.id.textTropheeNom);
        nameTrophy.setText(trophy.getName());

        TextView descTrophy = (TextView) findViewById(R.id.textTropheeDescription);
        descTrophy.setText(trophy.getDescription());

        ImageView imageView = (ImageView) findViewById(R.id.imageTrophee);
        Integer keyImg= this.getResources().getIdentifier(trophy.getImage(), "drawable", this.getPackageName());
        imageView.setImageResource(keyImg);

        buttonRetour = (Button) findViewById(R.id.buttonTropheeReturn);
        buttonRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

    }

    public void goBack() {
        Intent intent = new Intent(this, StatisticsActivity.class);
        startActivity(intent);
    }
}

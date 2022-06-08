package pns.si3.ihm.birder.views.trophies;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.models.Trophy;

public class TrophyActivity extends AppCompatActivity {

    Button buttonRetour;
    Button shareTrophy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trophy);
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
        buttonRetour.setOnClickListener(v -> goBack());

        shareTrophy = findViewById(R.id.buttonTropheeShare);
        shareTrophy.setOnClickListener(view -> {
            String shareBody = "J'ai obtenue le trophée \"" + trophy.getName() + "\" sur l'application Birder!";
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Le trophée");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Partager via"));
        });

    }

    public void goBack() {
        Intent intent = new Intent(this, StatisticsActivity.class);
        startActivity(intent);
    }
}

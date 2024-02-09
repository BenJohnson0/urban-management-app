package com.example.urban_management_app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.bumptech.glide.Glide;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.location.Address;
import android.location.Geocoder;
import android.content.Context;
import android.widget.Toast;

public class DetailedReportActivity extends AppCompatActivity {

    private TextView titleTextView, locationTextView, sizeTextView, urgencyTextView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_report);

        titleTextView = findViewById(R.id.titleTextView);
        locationTextView = findViewById(R.id.locationTextView);
        sizeTextView = findViewById(R.id.sizeTextView);
        urgencyTextView = findViewById(R.id.urgencyTextView);
        imageView = findViewById(R.id.reportImageView);

        Button amendButton = findViewById(R.id.amendButton);

        // retrieve report_id from the intent
        String reportId = getIntent().getStringExtra("report_id");

        // create a Firebase database reference to the specific report
        DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference("reports").child(reportId);

        reportRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Report report = dataSnapshot.getValue(Report.class);

                if (report != null) {
                    titleTextView.setText(report.getTitle());

                    // geocode coordinates to get an address
                    String address = geocodeLocation(DetailedReportActivity.this, report.getXCoordinates(), report.getYCoordinates());
                    locationTextView.setText("Near " + address);
                    sizeTextView.setText("The reported size is '" + report.getSize()+"'");
                    urgencyTextView.setText("The reported urgency is '" + report.getUrgency()+"'");

                    Glide.with(DetailedReportActivity.this)
                            .load(report.getImageUrl())
                            .into(imageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // TODO: handle database read error
            }
        });

        amendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAmendOrDeleteDialog(reportId); // pass the reportId to the dialog
            }
        });
    }

    // geocode latitude and longitude coordinates into an address
    private String geocodeLocation(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String addressText = "Unknown Location";

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                addressText = address.getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return addressText;
    }

    private void showAmendOrDeleteDialog(final String reportId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Amend or Delete Report");
        builder.setMessage("Are you sure you want to amend or delete this report?");
        builder.setPositiveButton("Amend", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Start the AmendReportActivity
                Intent intent = new Intent(DetailedReportActivity.this, AmendReportActivity.class);
                intent.putExtra("report_id", reportId);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteReport(reportId);
            }
        });
        builder.show();
    }

    // function for deleting the report
    private void deleteReport(String reportId) {
        DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference("reports").child(reportId);
        reportRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(DetailedReportActivity.this, "Report deleted", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DetailedReportActivity.this, HomeActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DetailedReportActivity.this, "Error deleting report, please try again later.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DetailedReportActivity.this, HomeActivity.class));
            }
        });
    }
}

package cloud.goober.gooberguard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText domainInput;
    private Button addDomainButton;
    private TextView noDomainsText;
    private RecyclerView domainRecyclerView;
    private DomainAdapter domainAdapter;
    private ArrayList<String> blockedDomains;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        domainInput = findViewById(R.id.domainInput);
        addDomainButton = findViewById(R.id.addDomainButton);
        noDomainsText = findViewById(R.id.noDomainsText);
        domainRecyclerView = findViewById(R.id.domainRecyclerView);

        blockedDomains = new ArrayList<>();
        domainAdapter = new DomainAdapter(blockedDomains);

        domainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        domainRecyclerView.setAdapter(domainAdapter);

        addDomainButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                String domain = domainInput.getText().toString().trim();
                if (!domain.isEmpty()) {
                    blockedDomains.add(domain);
                    domainAdapter.notifyDataSetChanged();
                    updateUI();
                    domainInput.setText("");
                }
            }
        });
    }

    private void updateUI() {
        if (blockedDomains.isEmpty()) {
            noDomainsText.setVisibility(View.VISIBLE);
            domainRecyclerView.setVisibility(View.GONE);
        } else {
            noDomainsText.setVisibility(View.GONE);
            domainRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}

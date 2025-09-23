package cloud.goober.gooberguard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int VPN_REQUEST_CODE = 123;

    private EditText domainInput;
    private Button addDomainButton;
    private Button vpnToggleButton;
    private TextView noDomainsText;
    private TextView vpnStatusText;
    private RecyclerView domainRecyclerView;
    private DomainAdapter domainAdapter;
    private ArrayList<String> blockedDomains;
    private DomainManager domainManager;
    private boolean isVpnRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        domainInput = findViewById(R.id.domainInput);
        addDomainButton = findViewById(R.id.addDomainButton);
        vpnToggleButton = findViewById(R.id.vpnToggleButton);
        noDomainsText = findViewById(R.id.noDomainsText);
        vpnStatusText = findViewById(R.id.vpnStatusText);
        domainRecyclerView = findViewById(R.id.domainRecyclerView);

        // Initialize domain manager and load existing domains
        domainManager = new DomainManager(this);
        blockedDomains = domainManager.getBlockedDomains();
        
        domainAdapter = new DomainAdapter(blockedDomains);
        domainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        domainRecyclerView.setAdapter(domainAdapter);

        // Set up button listeners
        addDomainButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                String domain = domainInput.getText().toString().trim();
                if (!domain.isEmpty()) {
                    domainManager.addBlockedDomain(domain);
                    blockedDomains.clear();
                    blockedDomains.addAll(domainManager.getBlockedDomains());
                    domainAdapter.notifyDataSetChanged();
                    updateUI();
                    domainInput.setText("");
                    
                    Toast.makeText(MainActivity.this, "Added: " + domain, Toast.LENGTH_SHORT).show();
                }
            }
        });

        vpnToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVpnRunning) {
                    stopVpnService();
                } else {
                    startVpnService();
                }
            }
        });

        updateUI();
    }

    private void updateUI() {
        if (blockedDomains.isEmpty()) {
            noDomainsText.setVisibility(View.VISIBLE);
            domainRecyclerView.setVisibility(View.GONE);
        } else {
            noDomainsText.setVisibility(View.GONE);
            domainRecyclerView.setVisibility(View.VISIBLE);
        }
        
        // Update VPN status and button text
        if (isVpnRunning) {
            vpnToggleButton.setText("Stop Protection");
            vpnStatusText.setText("Status: Protected - Blocking " + blockedDomains.size() + " domains");
            vpnStatusText.setTextColor(getColor(android.R.color.holo_green_dark));
        } else {
            vpnToggleButton.setText("Start Protection");
            vpnStatusText.setText("Status: Not Protected");
            vpnStatusText.setTextColor(getColor(android.R.color.holo_red_dark));
        }
    }

    private void startVpnService() {
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            // Request VPN permission
            startActivityForResult(intent, VPN_REQUEST_CODE);
        } else {
            // Permission already granted, start VPN
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
        }
    }

    private void stopVpnService() {
        Intent serviceIntent = new Intent(this, GooberVpnService.class);
        stopService(serviceIntent);
        isVpnRunning = false;
        updateUI();
        Toast.makeText(this, "VPN Protection Stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == VPN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Start the VPN service
                Intent serviceIntent = new Intent(this, GooberVpnService.class);
                startService(serviceIntent);
                isVpnRunning = true;
                updateUI();
                Toast.makeText(this, "VPN Protection Started", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "VPN permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

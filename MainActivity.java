package com.thaukpote.hti;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private EditText etName, etTickets, etWinningNo;
    private Spinner spPrizeType;
    private Button btnSave, btnCheck;
    private LinearLayout listContainer;
    private SharedPreferences sharedPreferences;
    private JSONArray ticketArray;

    private final String[] prizeTypes = {
            "ပထမဆု (သိန်း ၁၀၀၀၀)", "ဒုတိယဆု (သိန်း ၂၀၀)", "တတိယဆု (သိန်း ၁၀၀)",
            "ဆုကြီး (၃) ဆု၏ နောက်ဆုံးဂဏန်း (၅) လုံးတူ", "ဆုကြီး (၃) ဆု၏ နောက်ဆုံးဂဏန်း (၄) လုံးတူ",
            "ဆုကြီး (၃) ဆု၏ နောက်ဆုံးဂဏန်း (၃) လုံးတူ", "ဆုကြီး (၃) ဆု၏ +/- (၁) တူဆု",
            "အထူးဆု (၁၀ သိန်း)", "နှစ်သိမ့်ဆု (ပထမဆုကြီး၏ နောက်ဆုံးဂဏန်း ၂ လုံးတူ)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etTickets = findViewById(R.id.etTickets);
        etWinningNo = findViewById(R.id.etWinningNo);
        spPrizeType = findViewById(R.id.spPrizeType);
        btnSave = findViewById(R.id.btnSave);
        btnCheck = findViewById(R.id.btnCheck);
        listContainer = findViewById(R.id.listContainer);

        sharedPreferences = getSharedPreferences("ThaukPotePrefs", Context.MODE_PRIVATE);
        loadTickets();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, prizeTypes);
        spPrizeType.setAdapter(adapter);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String tickets = etTickets.getText().toString().trim();
            if (name.isEmpty() || tickets.isEmpty()) return;

            try {
                JSONObject obj = new JSONObject();
                obj.put("name", name);
                obj.put("tickets", tickets);
                ticketArray.put(obj);
                sharedPreferences.edit().putString("tickets_data", ticketArray.toString()).apply();
                loadTickets();
                etName.setText(""); etTickets.setText("");
            } catch (Exception e) { e.printStackTrace(); }
        });

        btnCheck.setOnClickListener(v -> {
            String winningNo = etWinningNo.getText().toString().trim();
            if (!winningNo.isEmpty()) checkWinners(winningNo, spPrizeType.getSelectedItemPosition());
        });
    }

    private void loadTickets() {
        listContainer.removeAllViews();
        try {
            ticketArray = new JSONArray(sharedPreferences.getString("tickets_data", "[]"));
            for (int i = 0; i < ticketArray.length(); i++) {
                final int index = i;
                JSONObject obj = ticketArray.getJSONObject(i);
                View itemView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
                TextView t1 = itemView.findViewById(android.R.id.text1);
                TextView t2 = itemView.findViewById(android.R.id.text2);
                t1.setText("ကံစမ်းသူ: " + obj.getString("name"));
                t2.setText("ထီလက်မှတ်များ: " + obj.getString("tickets"));
                itemView.setOnLongClickListener(v -> {
                    ticketArray.remove(index);
                    sharedPreferences.edit().putString("tickets_data", ticketArray.toString()).apply();
                    loadTickets();
                    return true;
                });
                listContainer.addView(itemView);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void checkWinners(String winNo, int prizeType) {
        boolean hasWinner = false;
        try {
            for (int i = 0; i < ticketArray.length(); i++) {
                JSONObject obj = ticketArray.getJSONObject(i);
                String[] myTickets = obj.getString("tickets").split(",");
                for (String ticket : myTickets) {
                    ticket = ticket.trim();
                    boolean isMatch = false;
                    if ((prizeType <= 2 || prizeType == 7) && ticket.equalsIgnoreCase(winNo)) isMatch = true;
                    else if (prizeType == 3 && ticket.endsWith(winNo.substring(Math.max(0, winNo.length() - 5)))) isMatch = true;
                    else if (prizeType == 4 && ticket.endsWith(winNo.substring(Math.max(0, winNo.length() - 4)))) isMatch = true;
                    else if (prizeType == 5 && ticket.endsWith(winNo.substring(Math.max(0, winNo.length() - 3)))) isMatch = true;
                    else if (prizeType == 8 && ticket.endsWith(winNo.substring(Math.max(0, winNo.length() - 2)))) isMatch = true;
                    
                    if (isMatch) { hasWinner = true; Toast.makeText(this, "🎉 ပေါက်ပြီ: " + obj.getString("name"), Toast.LENGTH_LONG).show(); }
                }
            }
            if (!hasWinner) Toast.makeText(this, "မဲမပေါက်ပါ", Toast.LENGTH_SHORT).show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}

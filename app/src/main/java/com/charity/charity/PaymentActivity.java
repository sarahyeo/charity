package com.charity.charity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.braintreepayments.api.Braintree;
import com.braintreepayments.api.dropin.BraintreePaymentActivity;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.net.MalformedURLException;

import cz.msebera.android.httpclient.Header;

public class PaymentActivity extends AppCompatActivity {
    private static final int PAYMENT_AUTH_REQ = 100;
    private String clientToken;

    // use later for ignoring operaitons until Braintree is setup? kinda like wait for Async task until it's done!
    private boolean isBraintreeSetup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            BMSClient.getInstance().initialize(this, "charity.mybluemix.net", "aeb6f4ec-822d-4cb0-b283-a5945db07248");
        }
        catch (MalformedURLException e) {
            // log error
        }

        AsyncHttpClient client = new AsyncHttpClient();
        // TODO: NEED SERVER
        client.get("http://charity.mybluemix.net/client_token", new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String clientToken) {
                PaymentActivity.this.clientToken = clientToken;
                Braintree.setup(PaymentActivity.this, clientToken, new Braintree.BraintreeSetupFinishedListener() {
                    @Override
                    public void onBraintreeSetupFinished(boolean setupSuccessful, Braintree braintree, String errorMessage, Exception exception) {
                        if (setupSuccessful) {
                            isBraintreeSetup = true;
                        }
                    }
                });
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                // log error
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void onBraintreeSubmit(View v) {
        if (isBraintreeSetup) {
            Intent intent = new Intent(getApplicationContext(), BraintreePaymentActivity.class);
            intent.putExtra(BraintreePaymentActivity.EXTRA_CLIENT_TOKEN, clientToken);
            startActivityForResult(intent, PAYMENT_AUTH_REQ);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PAYMENT_AUTH_REQ && resultCode == BraintreePaymentActivity.RESULT_OK) {
            String nonce = data.getStringExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);
            postNonceToServer(nonce);
        }
    }

    protected void postNonceToServer(String nonce) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("payment_method_nonce", nonce);

        // TODO: NEED SERVER
        client.post("http://charity.mybluemix.net/payment-methods", params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        //TODO: Handle response
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        // Do nothing; log error?
                    }
                }
        );
    }
}

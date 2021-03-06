package cm.aptoide.ptdev;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.Toast;

import cm.aptoide.ptdev.SpiceStuff.AlmostGenericResponseV2RequestListener;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.events.AppViewRefresh;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.FragmentComments;
import cm.aptoide.ptdev.fragments.callbacks.AddCommentCallback;
import cm.aptoide.ptdev.fragments.callbacks.AddCommentVoteCallback;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.AddApkCommentVoteRequest;
import cm.aptoide.ptdev.webservices.AddCommentRequest;
import cm.aptoide.ptdev.webservices.Errors;
import cm.aptoide.ptdev.webservices.json.GenericResponseV2;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by rmateus on 26-12-2013.
 */
public class AllCommentsActivity extends ActionBarActivity implements AddCommentCallback, AddCommentVoteCallback {

    private SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);
    private String repoName;
    private String versionName;
    private String packageName;
    private String token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_comments);

        FragmentComments fragmentComments = new FragmentComments();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, fragmentComments).commit();
        repoName = getIntent().getStringExtra("repoName");
        versionName = getIntent().getStringExtra("versionName");
        packageName = getIntent().getStringExtra("packageName");
        token = getIntent().getStringExtra("token");

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.comment_see_all));
    }

    public SpiceManager getSpice() {
        return spiceManager;
    }

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        spiceManager.shouldStop();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home || i == R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void addComment(String comment, String answerTo) {
        if (!PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getString("username", "NOT_SIGNED_UP").equals("NOT_SIGNED_UP")) {

            AddCommentRequest request = new AddCommentRequest(this);
            request.setApkversion(versionName);
            request.setPackageName(packageName);
            request.setRepo(repoName);
            request.setToken(token);
            request.setText(comment);

            if(answerTo != null) {
                request.setAnswearTo(answerTo);
            }

            if(comment.length()<10){
                Toast.makeText(getApplicationContext(), R.string.error_IARG_100, Toast.LENGTH_LONG).show();
                return;
            }


            spiceManager.execute(request, addCommentRequestListener);
            AptoideDialog.pleaseWaitDialog().show(getSupportFragmentManager(), "pleaseWaitDialog");
        } else {

            AptoideDialog.updateUsernameDialog().show(getSupportFragmentManager(), "updateNameDialog");

        }
    }

    RequestListener<GenericResponseV2> addCommentRequestListener =
            new AlmostGenericResponseV2RequestListener(this){
        @Override
        public void CaseOK() {
            Toast.makeText(Aptoide.getContext(), getString(R.string.comment_submitted), Toast.LENGTH_LONG).show();

            FragmentComments fragmentComments = new FragmentComments();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, fragmentComments).commit();
            setResult( RESULT_OK );
        }
    };

    @Override
    public void voteComment(int commentId, AddApkCommentVoteRequest.CommentVote vote) {
        AddApkCommentVoteRequest commentVoteRequest = new AddApkCommentVoteRequest();

        commentVoteRequest.setRepo(repoName);
        commentVoteRequest.setToken(token);
        commentVoteRequest.setCmtid(commentId);
        commentVoteRequest.setVote(vote);

        spiceManager.execute(commentVoteRequest, requestListener);
        AptoideDialog.pleaseWaitDialog().show(getSupportFragmentManager(), "pleaseWaitDialog");

    }

    RequestListener<GenericResponseV2> requestListener =
            new AlmostGenericResponseV2RequestListener(this){
        @Override
        public void CaseOK() {
            Toast.makeText(Aptoide.getContext(), getString(R.string.vote_submitted), Toast.LENGTH_LONG).show();

            FragmentComments fragmentComments = new FragmentComments();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, fragmentComments).commit();
            setResult( RESULT_OK );
        }
    };
}

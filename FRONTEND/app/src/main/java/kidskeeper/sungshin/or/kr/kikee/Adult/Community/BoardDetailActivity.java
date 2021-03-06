package kidskeeper.sungshin.or.kr.kikee.Adult.Community;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import kidskeeper.sungshin.or.kr.kikee.Model.request.BoardDetail;
import kidskeeper.sungshin.or.kr.kikee.Model.request.CommentWrite;
import kidskeeper.sungshin.or.kr.kikee.Model.request.Pick;
import kidskeeper.sungshin.or.kr.kikee.Model.response.BaseResult;
import kidskeeper.sungshin.or.kr.kikee.Model.response.BoardDetailResult;
import kidskeeper.sungshin.or.kr.kikee.Network.ApplicationController;
import kidskeeper.sungshin.or.kr.kikee.Network.NetworkService;
import kidskeeper.sungshin.or.kr.kikee.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardDetailActivity extends AppCompatActivity {

    @BindView(R.id.board_detail_title)
    TextView textViewTitle;
    @BindView(R.id.board_detail_content)
    TextView textViewContent;
    @BindView(R.id.board_detail_nickname)
    TextView textViewNickname;
    @BindView(R.id.board_detail_date)
    TextView textViewDate;
    @BindView(R.id.board_detail_hits)
    TextView textViewhits;
    @BindView(R.id.board_detail_picks)
    TextView textViewPicks;
    @BindView(R.id.comment_listView)
    ListView listViewComments;
    @BindView(R.id.board_detail_comment_write_button)
    Button buttonCommentWrite;
    @BindView(R.id.board_detail_comment_edit_text)
    EditText editTextComment;
    @BindView(R.id.board_detail_like_image)
    ImageView imageViewLike;

    private NetworkService service;
    private String board_idx;
    private String user_idx;
    CommentListAdapter commentListAdapter;
    private Boolean isPick = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        setContentView(R.layout.activity_board_detail);
        service = ApplicationController.getInstance().getNetworkService();
        ButterKnife.bind(this);
        loadData();

        clickEvent();
    }


    public void loadData() {
        SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
        user_idx = userInfo.getString("user_idx", "");
        board_idx = userInfo.getString("board_idx", "");
        BoardDetail data = new BoardDetail(board_idx, user_idx);

        Call<BoardDetailResult> getBoardDetailResult = service.getBoardDetailResult(data);
        getBoardDetailResult.enqueue(new Callback<BoardDetailResult>() {
            @Override
            public void onResponse(Call<BoardDetailResult> call, Response<BoardDetailResult> response) {
                if (response.isSuccessful()) {
                    String message = response.body().getMessage();
                    switch (message) {
                        case "SUCCESS":
                            textViewTitle.setText(String.valueOf(response.body().getBoard().getTitle()));
                            textViewContent.setText(String.valueOf(response.body().getBoard().getContent()));
                            textViewNickname.setText(String.valueOf(response.body().getBoard().getNickname()));
                            textViewDate.setText(String.valueOf(response.body().getBoard().getDate()));
                            textViewhits.setText(String.valueOf(response.body().getBoard().getHits()));
                            textViewPicks.setText(String.valueOf(response.body().getBoard().getPick()));
                            if (response.body().getIsPick().equals("0")) {
                                imageViewLike.setImageResource(R.drawable.unlike);
                                isPick = false;
                            }
                            commentListAdapter = new CommentListAdapter();
                            listViewComments.setAdapter(commentListAdapter);
                            for (int i = 0; i < response.body().getComments().size(); i++) {
                                commentListAdapter.addItem(response.body().getComments().get(i).getContent(), response.body().getComments().get(i).getNickname());
                            }

                    }
                }

            }

            @Override
            public void onFailure(Call<BoardDetailResult> call, Throwable t) {

            }
        });

    }

    public void clickEvent() {
        buttonCommentWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = editTextComment.getText().toString();
                if (comment.equals("")) {
                    Toast.makeText(getApplicationContext(), "댓글을 입력해 주세요", Toast.LENGTH_SHORT).show();
                } else {
                    CommentWrite data = new CommentWrite(board_idx, user_idx, comment);
                    Call<BaseResult> getCommentWtireReault = service.getCommentWriteResult(data);
                    getCommentWtireReault.enqueue(new Callback<BaseResult>() {
                        @Override
                        public void onResponse(Call<BaseResult> call, Response<BaseResult> response) {
                            if (response.isSuccessful()) {
                                String message = response.body().getMessage();
                                switch (message) {
                                    case "SUCCESS":
                                        SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = userInfo.edit();
                                        editor.putString("board_idx", board_idx);
                                        editor.commit();
                                        Intent intent = new Intent(getBaseContext(), BoardDetailActivity.class);
                                        intent.putExtra("idx", board_idx);
                                        startActivity(intent);
                                        finish();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<BaseResult> call, Throwable t) {

                        }
                    });
                }
            }
        });
        imageViewLike.setOnClickListener(new View.OnClickListener() {
            Pick data = new Pick(board_idx, user_idx);

            @Override
            public void onClick(View v) {
                if (isPick) {
                    Call<BaseResult> getUnPickResult = service.getUnPickResult(data);
                    getUnPickResult.enqueue(new Callback<BaseResult>() {
                        @Override
                        public void onResponse(Call<BaseResult> call, Response<BaseResult> response) {
                            if (response.isSuccessful()) {
                                String message = response.body().getMessage();
                                switch (message) {
                                    case "SUCCESS":
                                        imageViewLike.setImageResource(R.drawable.unlike);
                                        textViewPicks.setText(String.valueOf(Integer.parseInt(textViewPicks.getText().toString()) - 1));
                                        isPick = false;
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<BaseResult> call, Throwable t) {

                        }
                    });

                } else {
                    Call<BaseResult> getPickResult = service.getPickResult(data);
                    getPickResult.enqueue(new Callback<BaseResult>() {
                        @Override
                        public void onResponse(Call<BaseResult> call, Response<BaseResult> response) {
                            if (response.isSuccessful()) {
                                String message = response.body().getMessage();
                                switch (message) {
                                    case "SUCCESS":
                                        imageViewLike.setImageResource(R.drawable.like);
                                        textViewPicks.setText(String.valueOf(Integer.parseInt(textViewPicks.getText().toString()) + 1));
                                        isPick = true;
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<BaseResult> call, Throwable t) {

                        }
                    });

                }

            }
        });
    }

}
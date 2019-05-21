package android.beige.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_CHEATED_LIST = "has_cheated";
    private static final String KEY_ANSWERED = "answered_list";
    private static final String KEY_CORRECT = "answered_correct";
    private static final int REQUEST_CODE_CHEAT = 0;
    private Button mTrueButton;
    private Button mFalseButton;
    private Button mCheatButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private TextView mQuestionTextView;
    private Question[] mQuestionBank = new Question[]{
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true),
            new Question(R.string.question_australia, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_mitochondria, true),
            new Question(R.string.question_oceans, true)
    };
    private boolean[] mAnsweredList = new boolean[mQuestionBank.length];
    private boolean[] mCheatedList = new boolean[mQuestionBank.length];
    private int mQuestionIndex = 0;
    private int mNumCorrect = 0;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSavedInstanceState");
        savedInstanceState.putInt(KEY_INDEX, mQuestionIndex);
        savedInstanceState.putBooleanArray(KEY_ANSWERED, mAnsweredList);
        savedInstanceState.putInt(KEY_CORRECT, mNumCorrect);
        savedInstanceState.putBooleanArray(KEY_CHEATED_LIST, mCheatedList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate called");

        setContentView(R.layout.activity_quiz);
        mQuestionTextView = findViewById(R.id.question_text_view);
        mTrueButton = findViewById(R.id.true_button);
        mFalseButton = findViewById(R.id.false_button);
        mCheatButton = findViewById(R.id.cheat_button);
        mNextButton = findViewById(R.id.next_button);
        mPrevButton = findViewById(R.id.prev_button);

        if (savedInstanceState != null) {
            mQuestionIndex = savedInstanceState.getInt(KEY_INDEX);
            mAnsweredList = savedInstanceState.getBooleanArray(KEY_ANSWERED);
            mNumCorrect = savedInstanceState.getInt(KEY_CORRECT);
            mCheatedList = savedInstanceState.getBooleanArray(KEY_CHEATED_LIST);
        }

        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuestionIndex = (mQuestionIndex + 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(true);
                updateQuestion();
            }
        });

        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(false);
                updateQuestion();
            }
        });

        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean answerIsTrue = mQuestionBank[mQuestionIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuestionIndex = (mQuestionIndex + 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuestionIndex = (mQuestionIndex - 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        updateQuestion();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            mCheatedList[mQuestionIndex] = CheatActivity.wasAnswerShown(data);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    private void updateQuestion() {
        // Displays the question at the current index
        int question = mQuestionBank[mQuestionIndex].getTextResId();
        mQuestionTextView.setText(question);

        // Hides T/F buttons for questions that have already been answered
        if (mAnsweredList[mQuestionIndex]) {
            mTrueButton.setVisibility(View.INVISIBLE);
            mFalseButton.setVisibility(View.INVISIBLE);

            // Test if all questions have been answered
            boolean hasAnsweredAllQuestions = true;
            for (int i = 0; i < mQuestionBank.length; i++) {
                if (!mAnsweredList[i]) {
                    hasAnsweredAllQuestions = false;
                }
            }

            // If so, show a Toast with the percentage answered correctly
            if (hasAnsweredAllQuestions) {
                double score = ((double) mNumCorrect / mQuestionBank.length) * 100;
                score = Math.round(score * 10d) / 10d;
                String scoreString = getString(R.string.final_score, score);
                Toast scoreToast = Toast.makeText(this, scoreString, Toast.LENGTH_SHORT);
                scoreToast.setGravity(Gravity.TOP, 0, 0);
                scoreToast.show();
            }
        } else {
            mTrueButton.setVisibility(View.VISIBLE);
            mFalseButton.setVisibility(View.VISIBLE);
        }
    }

    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mQuestionIndex].isAnswerTrue();
        int messageResId;

        if (mCheatedList[mQuestionIndex]) {
            messageResId = R.string.judgement_toast;
        } else {
            if (userPressedTrue == answerIsTrue) {
                mNumCorrect++;
                messageResId = R.string.correct_toast;
            } else {
                messageResId = R.string.incorrect_toast;
            }
        }

        mAnsweredList[mQuestionIndex] = true;

        Toast t = Toast.makeText(this, messageResId, Toast.LENGTH_SHORT);
        t.setGravity(Gravity.TOP, 0, 0);
        t.show();
    }
}

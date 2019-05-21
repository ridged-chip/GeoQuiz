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
    private static final String KEY_CHEATED = "has_cheated";
    private static final String KEY_ANSWERED = "answered_list";
    private static final String KEY_CORRECT = "answered_correct";
    private static final int REQUEST_CODE_CHEAT = 0;

    private TextView mQuestionTextView;
    private Button mTrueButton;
    private Button mFalseButton;
    private Button mCheatButton;
    private TextView mCheatsRemainingTextView;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;

    private int mQuestionIndex = 0;
    private Question[] mQuestions = new Question[]{
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true),
            new Question(R.string.question_australia, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_mitochondria, true),
            new Question(R.string.question_oceans, true)
    };
    private boolean[] mQuestionsAnswered = new boolean[mQuestions.length];
    private boolean[] mQuestionsCheated = new boolean[mQuestions.length];
    private int mQuestionsCorrect;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSavedInstanceState");
        savedInstanceState.putInt(KEY_INDEX, mQuestionIndex);
        savedInstanceState.putBooleanArray(KEY_ANSWERED, mQuestionsAnswered);
        savedInstanceState.putInt(KEY_CORRECT, mQuestionsCorrect);
        savedInstanceState.putBooleanArray(KEY_CHEATED, mQuestionsCheated);
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
        mCheatsRemainingTextView = findViewById(R.id.cheats_remaining);
        mNextButton = findViewById(R.id.next_button);
        mPrevButton = findViewById(R.id.prev_button);

        if (savedInstanceState != null) {
            mQuestionIndex = savedInstanceState.getInt(KEY_INDEX);
            mQuestionsCorrect = savedInstanceState.getInt(KEY_CORRECT);
            mQuestionsAnswered = savedInstanceState.getBooleanArray(KEY_ANSWERED);
            mQuestionsCheated = savedInstanceState.getBooleanArray(KEY_CHEATED);
        }

        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuestionIndex = (mQuestionIndex + 1) % mQuestions.length;
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
                boolean answerIsTrue = mQuestions[mQuestionIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });


        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuestionIndex = (mQuestionIndex + 1) % mQuestions.length;
                updateQuestion();
            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuestionIndex = (mQuestionIndex - 1) % mQuestions.length;
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
            mQuestionsCheated[mQuestionIndex] = CheatActivity.wasAnswerShown(data);
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
        int question = mQuestions[mQuestionIndex].getTextResId();
        boolean questionWasAnswered = mQuestionsAnswered[mQuestionIndex];
        int numAnswered = numTrueInArray(mQuestionsAnswered);
        String cheatsRemainingString = getString(R.string.cheats_remaining, cheatsRemaining());

        // Displays the question at the current index
        mQuestionTextView.setText(question);

        // Hides Cheat Button if user has used all cheats
        if (cheatsRemaining() <= 0) mCheatButton.setVisibility(View.INVISIBLE);

        // Displays the number of cheats remaining
        mCheatsRemainingTextView.setText(cheatsRemainingString);

        // Hides T/F buttons for questions that have already been answered
        if (questionWasAnswered) {
            mTrueButton.setVisibility(View.INVISIBLE);
            mFalseButton.setVisibility(View.INVISIBLE);

            // If all questions are answered, show the result
            if(numAnswered == mQuestions.length) {
                double score = ((double) mQuestionsCorrect / mQuestions.length) * 100;
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

    private void checkAnswer(boolean userResponse) {
        boolean correctAnswer = mQuestions[mQuestionIndex].isAnswerTrue();
        boolean userCheated = mQuestionsCheated[mQuestionIndex];
        int messageResId;

        mQuestionsAnswered[mQuestionIndex] = true;

        if (userResponse == correctAnswer) {
            mQuestionsCorrect++;
            messageResId = R.string.correct_toast;
        } else messageResId = R.string.incorrect_toast;

        if (userCheated) messageResId = R.string.judgement_toast;

        Toast t = Toast.makeText(this, messageResId, Toast.LENGTH_SHORT);
        t.setGravity(Gravity.TOP, 0, 0);
        t.show();
    }

    private int numTrueInArray(boolean[] array) {
        int numTrue = 0;

        for (boolean b : array) {
            if (b) numTrue++;
        }
        return numTrue;
    }

    private int cheatsRemaining() {
        return 3 - numTrueInArray(mQuestionsCheated);
    }
}

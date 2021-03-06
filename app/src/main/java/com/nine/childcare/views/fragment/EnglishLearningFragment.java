package com.nine.childcare.views.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.databinding.tool.util.StringUtils;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.nine.childcare.R;
import com.nine.childcare.databinding.EnglishLearningFragmentBinding;
import com.nine.childcare.viewmodel.EnglishLearningViewModel;

import java.util.ArrayList;

public class EnglishLearningFragment extends BaseFragment<EnglishLearningFragmentBinding, EnglishLearningViewModel> {

    @Override
    protected Class<EnglishLearningViewModel> getViewModelClass() {
        return EnglishLearningViewModel.class;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.english_learning_fragment;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        mViewModel.getSearchWord().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.englishWordMean.setText("");
                handleMeanString(s);
            }
        });
        binding.btnSearchEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.getWordFromDataBase(binding.englishWordSearch.getText().toString().trim());
                hideKeyBoard();
            }
        });

        binding.btnSpeakEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start a recognize speech
                Intent speakIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speakIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speakIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");
                try {
                    speakActivityResultLauncher.launch(speakIntent);
                } catch (ActivityNotFoundException e) {
                    makeToast("Your device does not support Speech to Text");
                    e.printStackTrace();
                }
            }
        });
    }

    // get result of recognize speech intent
    private final ActivityResultLauncher<Intent> speakActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        binding.englishWordSearch.setText(matches.get(0));
                        mViewModel.getWordFromDataBase(matches.get(0));
                    }
                }
            }
        }
    );

    // deal with string
    private void handleMeanString (String mean) {
        String[] split = mean.substring(1).split("\n");
        for (String s : split) {
            Spannable word = new SpannableString("\n" + StringUtils.capitalize(s.substring(1).trim().replace("+", "\n-")));
            if (s.trim().startsWith("*")) {
                word.setSpan(new ForegroundColorSpan(Color.BLUE), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (s.trim().startsWith("-")) {
                word.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (s.trim().startsWith("=")) {
                word.setSpan(new ForegroundColorSpan(Color.BLACK), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (s.trim().startsWith("!")) {
                word.setSpan(new ForegroundColorSpan(Color.RED), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            binding.englishWordMean.append(word);
        }
    }
}

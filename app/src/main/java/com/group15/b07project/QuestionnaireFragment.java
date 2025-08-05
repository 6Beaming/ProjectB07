package com.group15.b07project;

import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

// Modularize the questionnaire into three sections(pages)
// so that it'll be convenient for them to navigate their answers and edit them later on
// Submit button is on the last page, which guarantees users have answered all the questions before the last page
// Note that the user must answer all the questions from Warm-Up, Branch-Specific, and Follow-Up sections.
public class QuestionnaireFragment extends Fragment {
    private enum Page { WARMUP, BRANCH, FOLLOWUP } // Use enumeration type to track different pages
    private Page currentPage = Page.WARMUP;
    private LinearLayout container; // holds question views
    private Button btnSubmit;
    private Button btnPrev;
    private Button btnNext;
    private QuestionsBundle qBundle; // parsed JSON holder
    private final Map<String, Object> answers = new HashMap<>(); // key = question ID;
                                                                // value = String (single-choice, dropdown, date, or text answers)
                                                                //          or List<String> (multiple-choice answers)
    private int brown_grey;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view;
        // Convert xml layout file to View objects
        view = inflater.inflate(R.layout.fragment_questionnaire, parent, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Bind views
        container = view.findViewById(R.id.container);
        btnPrev = view.findViewById(R.id.btnPrev);
        btnNext = view.findViewById(R.id.btnNext);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        brown_grey = ContextCompat.getColor(requireContext(), R.color.browngrey);
        btnPrev.setOnClickListener(v -> goToPage(previousPage())); // You can go to previous page
                                                                        // even if you haven't answered current page's answer
        // If no all answers on this page is answered -> a red text warning will be inserted below the question
        btnNext.setOnClickListener(v -> {
            if (validatePage()) {
                goToPage(nextPage());
            }
        });
        btnSubmit.setOnClickListener(v -> {
            if (validatePage()) {
                onSubmit();
            }
        });

        loadQuestions();   // parse questions.json
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            goToPage(Page.WARMUP);
            return;
        }

        String uid = user.getUid();
        DatabaseReference qRef = FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(uid)
                .child("questionnaire");

        qRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                @SuppressWarnings("unchecked")
                Map<String,Object> saved = (Map<String,Object>) snap.getValue();
                // get saved answers if any
                if (saved != null && !saved.isEmpty()) {
                    answers.putAll(saved);
                }
                goToPage(Page.WARMUP);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                goToPage(Page.WARMUP);
            }
        });
    }

    //Load and parse JSON from assets into qBundle
    private void loadQuestions() {
        // cannot use a File type because it's not a real file in terms of filesystem
        // use getContext to get the Activity’s context; open json from assets using getAssets().open
        // use try-with-resources to automatically closes the InputStream when done; open and read json file
        try (InputStream is = requireContext().getAssets().open("questions.json");
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            // parse json into qBundle
            Gson gson = new Gson();
            qBundle = gson.fromJson(reader, QuestionsBundle.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Page previousPage() {
        if (currentPage == Page.BRANCH) {
            return Page.WARMUP; // On Branch page when you hit Prev, go to Warmup
        }
        else if (currentPage == Page.FOLLOWUP) {
            return Page.BRANCH; // On Followup page when you hit Prev, go to Branch
        }
        return null;
    }

    private Page nextPage() {
        if (currentPage == Page.WARMUP) {
            return Page.BRANCH; // On Warmup page when you hit Next, you go to Branch
        }
        else if (currentPage == Page.BRANCH) {
            return Page.FOLLOWUP;  // On Branch page when you hit Next, go to Followup
        }
        return null;
    }

    // Render the specified page
    private void goToPage(Page page) {
        if (page == null) return;

        currentPage = page; // update currentPage
        container.removeAllViews(); // clear old views so that each page works for their own type

        if (page == Page.WARMUP) {
            for (Question q : qBundle.warmUp) {
                container.addView(createViewForQuestion(q));  // add each warm-up question to views
            }
            // Warmup page only shows Next button
            btnPrev.setVisibility(View.GONE);
            btnNext.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.GONE);
        }
        else if (page == Page.BRANCH) { // now status is non-null, and you can go to Branch page
            String status = (String) answers.get("status");
            // after you hit the button, validatePage will validate if you have answered all of the questions
            // so your status will surely be chosen
            List<Question> branchQs = qBundle.branch.get(status); // get the specified branch questions
            if (branchQs != null) {  // verify the status exists
                for (Question q : branchQs)
                    container.addView(createViewForQuestion(q)); // add each branch question to views
            }
            // Branch page shows: Previous, Next
            btnPrev.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.GONE);
        }
        else { // FOLLOWUP
            for (Question q : qBundle.followUp) {
                container.addView(createViewForQuestion(q)); // add each follow up question to views
            }
            // Followup page shows: Previous, Submit
            btnPrev.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.VISIBLE);
        }
    }

    // Validate every question on the current page.
    // If any is missing, show an inline error under it and return false.
    private boolean validatePage() {
        List<Question> pageQs = new ArrayList<>();
        // replace switch-case with if-else
        if (currentPage == Page.WARMUP) {
            pageQs = qBundle.warmUp; // qBundle.warmUp is a 'List' of Question
        }
        else if (currentPage == Page.BRANCH) {
            String status = (String) answers.get("status");
            // if your currentPage is Branch, that means you have answered WarmUp questions;
            // otherwise the button won't allow you to switch currentPage
            // so status must be answered, i.e. not null
            pageQs = qBundle.branch.get(status); // qBundle.branch is a Map<String, List<Question>>
                                                 // String(status) is the key
            if (pageQs ==null)
                pageQs = new ArrayList<>(); // avoid NullPointer exception
        }
        else if (currentPage == Page.FOLLOWUP) {
            pageQs = qBundle.followUp;
        }

        boolean ifAllAnswered = true;

        for (Question q : pageQs) {
            Object ans = answers.get(q.id);
            if (ans == null) { // answer is missing including Date type, single choice(including single+text)
                showErrorForQuestion(q);
                ifAllAnswered = false;
            }
            else if (ans instanceof String && ((String) ans).trim().isEmpty()) { // if text type q not answered
                showErrorForQuestion(q);
                ifAllAnswered = false;
            }
            else if (ans instanceof List && ((List<?>) ans).isEmpty()) { // if empty list (multiples q not answered)
                showErrorForQuestion(q);
                ifAllAnswered = false;
            }
            else if ("single+text".equals(q.type) && "Yes".equals(answers.get(q.id))){ // if single+text, but
                Object text = answers.get(q.id + "_text");
                if ((text == null) ||((String) text).trim().isEmpty()) { // if text is missing or text is just whitespace
                    showErrorForQuestion(q);
                    ifAllAnswered = false;
                }
            }
        }

        return ifAllAnswered;
    }


    // Remove the error message("This question is required") as soon as users answer the question
    // instead of waiting until you press the Next/Submit button
    // This method is called from each input’s listener, immediately after you save the answer
    private void clearErrorForQuestion(Question q) {
        LinearLayout wrap = container.findViewWithTag("question_" + q.id);
        if (wrap == null) return;
        View err = wrap.findViewWithTag("error_" + q.id);
        if (err != null)
            wrap.removeView(err);
    }


    // Show a red "This question is required." under the given question
    private void showErrorForQuestion(Question q) {
        LinearLayout qLayout = container.findViewWithTag("question_" + q.id);
        if (qLayout == null) return;

        // remove old if present
        View old = qLayout.findViewWithTag("error_" + q.id);
        if (old != null)
            qLayout.removeView(old);

        // add new view
        TextView err = new TextView(getContext());
        err.setText(R.string.this_question_is_required);
        err.setTextColor(0xFFFF0000);  // red
        err.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f); // set error message font size to 13sp
        err.setTag("error_" + q.id);
        qLayout.addView(err);
    }


    // Wrap question UI in a LinearLayout tagged for error handling
    private View createViewForQuestion(Question q) { // This return view will be added into container
        // Create the wrapper
        LinearLayout wrap = new LinearLayout(getContext());
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setTag("question_" + q.id);

        // Give it some bottom margin so questions don’t butt up against each other :)
        int marginDp = 24;
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                marginDp,
                getResources().getDisplayMetrics()
        );
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, 0, 0, px);
        wrap.setLayoutParams(lp);

        // Add the question label
        TextView tv = new TextView(getContext());
        tv.setText(q.text);
        tv.setTextColor(brown_grey);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f); // set question font size to 18sp
        wrap.addView(tv);
        // add input view
        if ("single".equals(q.type) || "single+text".equals(q.type)) {
            wrap.addView(createSingle(q));
        }
        else if ("multiple".equals(q.type)) {
            wrap.addView(createMultiple(q));
        }
        else if ("dropdown".equals(q.type)) {
            wrap.addView(createDropdown(q));
        }
        else {
            wrap.addView(createText(q));
        }
        return wrap;
    }


    // Input renderers
    private View createSingle(Question q) {
        LinearLayout singleWrap = new LinearLayout(getContext());
        singleWrap.setOrientation(LinearLayout.VERTICAL);

        Object saved = answers.get(q.id); // previously selected value, if any
        RadioGroup rg = new RadioGroup(getContext());
        for (String opt : q.options) {
            RadioButton rb = new RadioButton(getContext());
            rb.setText(opt);
            rb.setTextColor(brown_grey);
            rb.setId(View.generateViewId()); // unique ID per button!!!
            rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f); // set font size to 16 sp

            if (opt.equals(saved)) {
                rb.setChecked(true);  // pre‑select if we have a saved answer
            }
            rg.addView(rb);
        }
        // When the user selects a radio button(physically tap one), it will
        // Unchecks the previously selected button, Checks the new one
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selected_button = group.findViewById(checkedId);
            String selected = selected_button.getText().toString();
            answers.put(q.id, selected);
            clearErrorForQuestion(q);

            // show/hide follow‑up field if needed
            View follow = singleWrap.findViewWithTag(q.id + "_text");
            if (follow != null) {
                follow.setVisibility("Yes".equals(selected) ? View.VISIBLE : View.GONE);
            }
        });
        singleWrap.addView(rg);

        if ("single+text".equals(q.type)) {
            EditText et = new EditText(getContext());
            et.setHint(q.followupTextPrompt);
            et.setHintTextColor(brown_grey);
            et.setTextColor(brown_grey);
            et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f); // set prompt font size to 16sp
            et.setTag(q.id + "_text");

            // pre‑fill if we previously saved text
            Object prev = answers.get(q.id + "_text");
            if (prev != null) {
                et.setText(prev.toString());
                et.setHintTextColor(brown_grey);
                et.setTextColor(brown_grey);
            }
            // Show the text block only if the answer is "Yes"
            et.setVisibility("Yes".equals(saved) ? View.VISIBLE : View.GONE);
            et.addTextChangedListener(new SimpleTextWatcher(s -> {
                answers.put(q.id + "_text", s); // store the input text into answers
                clearErrorForQuestion(q);
            }));

            singleWrap.addView(et);
        }
        return singleWrap;
    }

    private LinearLayout createMultiple(Question q) {
        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        @SuppressWarnings("unchecked") List<String> saved = (List<String>) answers.get(q.id);
        for (String opt : q.options) {
            CheckBox cb = new CheckBox(getContext());
            cb.setText(opt);
            cb.setTextColor(brown_grey);
            cb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f); // set font size to 16 sp
            if (saved != null && saved.contains(opt)) cb.setChecked(true);
            cb.setOnCheckedChangeListener((b,checked) -> {
                List<String> list = new ArrayList<>();
                @SuppressWarnings("unchecked") List<String> ex= (List<String>)answers.get(q.id);
                if (ex!=null)
                    list=ex; // pre-fill the answer
                if (checked)
                    list.add(opt);
                else
                    list.remove(opt);
                answers.put(q.id, list);
                clearErrorForQuestion(q);
            });
            ll.addView(cb);
        }
        return ll;
    }

    private Spinner createDropdown(Question q) {
        Spinner spinner = new Spinner(requireContext());
        String saved = (String)answers.get(q.id);

        // Build a list with a prompt at the last index
        List<String> opts = new ArrayList<>(q.options); // add the question options
        int index = opts.indexOf(saved);
        opts.add("Select a city");  // prompt as last item

        // Create an ArrayAdapter that uses your custom layouts
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), R.layout.custom_questionnaire_spinner, opts) {
            @Override
            public int getCount() { // tells the spinner how many items are in the dropdown list
                // Hide the last item(the prompt) from the dropdown view
                return super.getCount() - 1; // e.g. opts = ["A","B","prompt"] super.getCount() returns 3 -> get.Count() returns 2
                                            // only the first 2 items (index 0 to 1) are shown in the dropdown.
                                            // but the last item is not gone in opts
            }
        };
        adapter.setDropDownViewResource(R.layout.custom_questionnaire_spinner); // dropdown layout
        spinner.setAdapter(adapter);

        if (index != -1) // saved answers -> show the saved answer in the spinner
            spinner.setSelection(index, false);
        else // no saved answers -> show prompt initially
            spinner.setSelection(adapter.getCount(), false);
        // adapter.getCount() now gives the last index == opts.size() - 1

        // Install a listener that ignores the dummy prompt at pos 0(the prompt)
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // e.g. opts = ["A","B","prompt"]
                if (pos < opts.size() - 1) {
                    // the city (pos 0, 1) < 3(opts.size)-1
                    answers.put(q.id, opts.get(pos));
                    clearErrorForQuestion(q);
                } else { // pos == 2, prompt selected as no answer
                    answers.remove(q.id);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return spinner;
    }

    private EditText createText(Question q) {
        EditText et = new EditText(getContext());
        et.setHint(q.followupTextPrompt);
        et.setTextColor(brown_grey);
        et.setHintTextColor(brown_grey);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f); // set prompt font size to 16sp
        if ("date".equals(q.type))
            et.setInputType(InputType.TYPE_CLASS_DATETIME);  // change the keyboard layout to digits
        Object txt=answers.get(q.id);
        if(txt!=null)
            et.setText(txt.toString()); // pre-fill
        et.addTextChangedListener(new SimpleTextWatcher(s -> {
            answers.put(q.id, s);
            clearErrorForQuestion(q);
        }));
        return et;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    // Store answers under users/{uid}/questionnaire in Firebase
    private void onSubmit() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // get user
        if (user == null) { // not signed in
            Toast.makeText(getContext(),"Login required to submit", Toast.LENGTH_SHORT).show();
            return;   // stop submit
        }
        String uid = user.getUid();  // get UID
        // DB ref
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        DatabaseReference ref = userRef.child("questionnaire");
        // Write answers
        ref.setValue(answers)
                .addOnSuccessListener(a -> {
                    Toast.makeText(getContext(), "Saved! Your safety plan is ready.", Toast.LENGTH_SHORT).show();
                    userRef.child("newUser").setValue(false); // now that user has answered the questionnaire, they're no more new user
                                                                        // use this as a way to check if Questionnaire needs to be initiated
                    loadFragment(new HomeFragment());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
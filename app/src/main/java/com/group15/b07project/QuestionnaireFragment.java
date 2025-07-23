package com.group15.b07project;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

// Divide the questionnaire into three sections(pages) and submit button is on the last page,
// so that it'll be convenient for them to navigate their answers and edit them later on
public class QuestionnaireFragment extends Fragment {
    private enum Page { WARMUP, BRANCH, FOLLOWUP } // Use enumeration type to track different pages
    private Page currentPage = Page.WARMUP;
    private LinearLayout container; // holds question views
    private Button btnSubmit; // submit button
    private Button btnPrev; // previous button
    private Button btnNext; // next button
    private QuestionsBundle qBundle; // parsed JSON holder
    private final Map<String, Object> answers = new HashMap<>(); // user’s answers, Object as in String for single answer q;
                                                                 //                             List<String> for multiple answers

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view;
        // Convert xml layout file to View objects
        view = inflater.inflate(R.layout.fragment_questionnaire, parent, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {  // after view exists
        // Bind views
        container = view.findViewById(R.id.container);
        btnPrev = view.findViewById(R.id.btnPrev);
        btnNext = view.findViewById(R.id.btnNext);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        // Handle button clicks
        btnPrev.setOnClickListener(v -> goToPage(previousPage()));
        btnNext.setOnClickListener(v -> goToPage(nextPage()));
        btnSubmit.setOnClickListener(v -> onSubmit());

        loadQuestions();   // parse questions.json
        goToPage(Page.WARMUP);    // show warm‑up questions
    }

    //Note that the user must answer all the questions from Warm-Up, Branch-Specific, and Follow-Up sections.

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

    // Return what will be the previous page
    private Page previousPage() {
        if (currentPage == Page.BRANCH) {
            return Page.WARMUP; // On Branch page when you hit Prev, go to Warmup
        }
        else if (currentPage == Page.FOLLOWUP) {
            return Page.BRANCH; // On Followup page when you hit Prev, go to Branch
        }
        return null; // no previous
    }

    // Return what will be the next page
    private Page nextPage() {
        if (currentPage == Page.WARMUP) {
            return Page.BRANCH; // On Warmup page when you hit Next, you go to Branch
        }
        else if (currentPage == Page.BRANCH) {
            return Page.FOLLOWUP;  // On Branch page when you hit Next, go to Followup
        }
        return null; // no next
    }

    // Render the specified page
    private void goToPage(Page page) {
        if (page == null) return; // nothing to do
        // If trying to go to BRANCH without answering status, bounce back
        String status = (String) answers.get("status");      // get selected status
        if (page == Page.BRANCH && status == null) { // trying to go to Branch page, but
                                                    // no status :( --> prompt to select
            Toast.makeText(getContext(),"Please select your status first", Toast.LENGTH_SHORT).show();
            goToPage(Page.WARMUP); // redirect to warm-up
            return;  // stop further processing
        }

        currentPage = page; // update currentPage
        container.removeAllViews(); // clear old views so that each page works for their own type

        if (page == Page.WARMUP) {
            for (Question q : qBundle.warmUp) {
                container.addView(createViewForQuestion(q));  // add each warm-up question to views
            }
            // Warmup page only shows Next button
            btnNext.setVisibility(View.VISIBLE);
        }
        else if (page == Page.BRANCH) { // now status is non-null, and you can go to Branch page
            List<Question> branchQs = qBundle.branch.get(status); // get the specified branch questions
            if (branchQs != null) {  // verify the status exists
                for (Question q : branchQs)
                    container.addView(createViewForQuestion(q));
            }
            // Branch page shows: Previous, Next
            btnPrev.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.VISIBLE);
        }
        else if (page == Page.FOLLOWUP) {
            for (Question q : qBundle.followUp) {
                container.addView(createViewForQuestion(q)); // add each follow up question to views
            }
            // Followup page shows: Previous, Submit
            btnPrev.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.VISIBLE);
        }

        btnSubmit.setEnabled(allAnswered()); // Enable submit button if all questions are answered
    }


//    private void WarmUp() {
//        //qBundle.warmup is a List of Question
//        for (Question q : qBundle.warmUp) {
//            View qv = createViewForQuestion(q);  // build its views
//            container.addView(qv); // add each qv to UI
//            if ("status".equals(q.id)) {                               // detect status field
//                // qv is already a RadioGroup, so cast it:
//                RadioGroup rg = (RadioGroup) qv;
//                rg.setOnCheckedChangeListener((group, checkedId) -> {  // on status change
//                    String sel = ((RadioButton)
//                            group.findViewById(checkedId))
//                            .getText().toString();                           // get choice text
//                    answers.put("status", sel);                        // save status
//                    BranchAndFollowUp();   // show branch & followUp questions
//                });
//            }
//        }
//    }

    /* Previous method:
     * After status selected, render branch‑specific + follow‑up questions
     * [ status question ]
     * [ city question   ]
     * [ safe_room       ]
     * [ live_with       ]
     * [ children        ]
     * [ Submit button   ]
     *  As soon as status is answered, the branch and follow‑up questions are injected below:
     * [ status          ]
     * [ city            ]
     * [ safe_room       ]
     * [ live_with       ]
     * [ children        ]
     *  ———————————————
     * [ branch Q1       ]
     * [ branch Q2       ]
     * [ branch Q3       ]
     *  ———————————————
     * [ followUp Q1     ]
     * [ Submit button   ]
     * So the previous questions won't disappear and users can choose to answer again
     */

//    private void BranchAndFollowUp() {
//        int warmUpCount = qBundle.warmUp.size(); // how many warmUp
//        while (container.getChildCount() > warmUpCount + 1) {  // clear old dynamic
//            container.removeViewAt(warmUpCount);  // keep submit button
//        }
//        String status = (String) answers.get("status");   // get selected status
//        List<Question> branchQs = qBundle.branch.get(status);  // corresponding questions
//        if (branchQs != null) {
//            for (Question q : branchQs) {
//                container.addView(createViewForQuestion(q));  // add its UI
//            }
//        }
//        for (Question q : qBundle.followUp) {  // then follow‑up
//            container.addView(createViewForQuestion(q)); // render it
//        }
//    }

    // Create UI for a single question
    private View createViewForQuestion(Question q) {
        TextView tv = new TextView(getContext());                 // create label
        tv.setText(q.text);                                       // set text
        container.addView(tv);                                    // add to layout

        if ("single".equals(q.type) || "single+text".equals(q.type)) { // single-choice
            RadioGroup rg = new RadioGroup(getContext());         // create group
            Object saved = answers.get(q.id);                    // get saved
            for (String opt : q.options) {                        // for each option
                RadioButton rb = new RadioButton(getContext());  // create radio
                rb.setText(opt);                                // set label
                if (saved != null && saved.equals(opt)) {       // pre-select
                    rb.setChecked(true);
                }
                rg.addView(rb);                                 // add to RG
            }
            rg.setOnCheckedChangeListener((group, checkedId) -> {// on select
                String sel = ((RadioButton)group.findViewById(checkedId)).getText().toString(); // get text
                answers.put(q.id, sel);                         // save
                btnSubmit.setEnabled(allAnswered());                            // update Submit
            });
            container.addView(rg);                               // add RG

            if ("single+text".equals(q.type)) {               // if has follow-up
                EditText et = new EditText(getContext());      // create field
                et.setHint(q.followupTextPrompt);              // set hint
                et.setTag(q.id+"_follow");                   // tag
                Object followSaved = answers.get(q.id+"_text"); // saved text
                if ("Yes".equals(answers.get(q.id))) {       // show if Yes
                    et.setVisibility(View.VISIBLE);           // visible
                    if (followSaved!=null) et.setText(followSaved.toString()); // set text
                }
                else {
                    et.setVisibility(View.GONE);              // hide otherwise
                }
                et.addTextChangedListener(new SimpleTextWatcher(s -> {// on text
                    answers.put(q.id+"_text",s);             // save
                    btnSubmit.setEnabled(allAnswered());                       // update Submit
                }));
                container.addView(et);                         // add field
            }
            return rg;                                           // return group
        }

        else if ("multiple".equals(q.type)) {                // multiple-choice
            LinearLayout ll = new LinearLayout(getContext());    // vertical layout
            ll.setOrientation(LinearLayout.VERTICAL);           // set orientation
            List<String> savedList = (List<String>) answers.get(q.id); // get saved, answers map q.id(String) to the answer(actual type is ArrayList<String>)
                                                                        // The cast only changes the reference type
            for (String opt : q.options) {
                CheckBox cb = new CheckBox(getContext());       // create checkbox
                cb.setText(opt);                                 // set label
                if (savedList!=null && savedList.contains(opt)) { // pre-check
                    cb.setChecked(true);
                }
                // Updates answers.get(q.id) based on the checkbox for each opt
                cb.setOnCheckedChangeListener((button,checked) -> { // on toggle
                    List<String> list = new ArrayList<>();
                    if (answers.get(q.id) != null) // update the list if there are answered stored
                        list = (List<String>) answers.get(q.id); // e.g. First time(null) empty -> add opt1 -> opt1
                                                                // Second time empty -> opt1(updated) -> add opt2 -> opt1,opt2
                    if(checked)
                        list.add(opt);                    // add if checked
                    else
                        list.remove(opt);                  // remove if unchecked, no exception if opt is not in list
                    answers.put(q.id, list);   // each time you do it, it mutates
                    btnSubmit.setEnabled(allAnswered());         // update Submit
                });
                ll.addView(cb); // add to ll
            }
            container.addView(ll);
            return ll;  // return the linear layout
        }

        else if ("dropdown".equals(q.type)) {                // dropdown
            Spinner sp = new Spinner(getContext());             // create spinner
            ArrayAdapter<String> ad=new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item,q.options); // adapter
            ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // set style
            sp.setAdapter(ad);                                   // attach adapter
            Object sel=answers.get(q.id);                        // get saved
            if(sel!=null){                                       // if exists
                int idx=q.options.indexOf(sel.toString());      // find index
                if(idx>=0) sp.setSelection(idx);                 // pre-select
            }
            sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                @Override public void onItemSelected(AdapterView<?> p,View v,int pos,long id){ // on select
                    answers.put(q.id,q.options.get(pos));       // save
                    btnSubmit.setEnabled(allAnswered());                           // update Submit
                }
                @Override public void onNothingSelected(AdapterView<?> p){}
            });
            container.addView(sp);                               // add spinner
            return sp;                                           // return spinner
        }

        else if ("text".equals(q.type)||"date".equals(q.type)) { // text or date
            EditText et= new EditText(getContext());              // create text field
            et.setHint(q.text);                                  // set hint
            if("date".equals(q.type))
                et.setInputType(InputType.TYPE_CLASS_DATETIME); // date input
            Object savedText=answers.get(q.id);                   // get saved
            if(savedText!=null)
                et.setText(savedText.toString()); // pre-fill
            et.addTextChangedListener(new SimpleTextWatcher(s->{ // on text change
                answers.put(q.id,s);                             // save
                btnSubmit.setEnabled(allAnswered());                              // update Submit
            }));
            container.addView(et);                               // add to layout
            return et;                                           // return field
        }                                                          // no matching type
        return tv;                                                // return TextView fallback
    }


    // Check all sections answered
    private boolean allAnswered() {
        // Create question sets that user is supposed to answer
        List<Question> all = new ArrayList<>(qBundle.warmUp); // initiate with qBundle.warmUp
                                                            // actual type for all is ArrayList
                                                            // qBundle.warmUp is a 'List' of Question
        String status = (String) answers.get("status");
        // Add other specific questions to the question set
        if (status != null) {
            List<Question> branchQs = qBundle.branch.get(status); // qBundle.branch is a Map<String, List<Question>>
                                                                // String status is the key
            if (branchQs != null) // check if the status exists
                all.addAll(branchQs);

            all.addAll(qBundle.followUp);
        }
        // If status is null, then there must exists a q.id == status answer.get(q.id) that is NULL
        // so it'll return false
        for (Question q : all) {
            Object ans = answers.get(q.id);
            if (ans == null) // answer is missing
                return false;
            if (ans instanceof String && ((String) ans).trim().isEmpty()) // if empty (single choice q not answered)
                return false;
            if (ans instanceof List && ((List<?>) ans).isEmpty()) // if empty list (multiples q not answered)
                return false;
            if ("single+text".equals(q.type)
                    && "Yes".equals(answers.get(q.id))
                    && ((String) answers.get(q.id + "_text")).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // Store answers under /questionnaires in Firebase
    private void onSubmit() {
        // if no all questions are answered, and users try to hit the submit button:
        // a text will prompt users to complete; no submission is made
        if (!allAnswered()) {
            // show prompt if no answer all the question
            Toast.makeText(getContext(),"Please complete all questions before submitting", Toast.LENGTH_SHORT).show();
            return; // stop submit
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // get user
        if (user == null) { // not signed in
            Toast.makeText(getContext(),"Login required to submit", Toast.LENGTH_SHORT).show();
            return;   // stop submit
        }
        String uid = user.getUid();                            // get UID
        // DB ref
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = database.getReference("users");
        DatabaseReference userRef = rootRef.child(uid);
        DatabaseReference ref = userRef.child("questionnaire");
        // Write answers
        ref.setValue(answers)
                .addOnSuccessListener(a ->Toast.makeText(getContext(), // on success
                        "Saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->Toast.makeText(getContext(), // on failure
                        "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}

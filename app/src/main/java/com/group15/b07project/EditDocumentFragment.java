package com.group15.b07project;

import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EditDocumentFragment extends BottomSheetDialogFragment {
    TextView title_input, description_input;
    Button confirm_button;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), R.style.BottomSheetTheme);
    }

    @Override
    public void onStart() {
        super.onStart();

        View bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Expand by default

            // Set height to 70% of screen
            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHeight = displayMetrics.heightPixels;

            ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
            layoutParams.height = (int) (screenHeight * 0.7); // 70% height
            bottomSheet.setLayoutParams(layoutParams);
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_document,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        title_input=view.findViewById(R.id.editText_title);
        description_input=view.findViewById(R.id.editText_description);
        confirm_button=view.findViewById(R.id.button_confirm_tile_description);

        Bundle args = getArguments();
        if (args != null) {
            String passedTitle = args.getString("file_title");    // passes from parent fragment
            String passedDescription = args.getString("file_description");

            if (passedTitle != null) {
                title_input.setText(passedTitle);
            }

            if (passedDescription != null) {
                description_input.setText(passedDescription);
            }
        }

        confirm_button.setOnClickListener(v -> {
            String title = title_input.getText().toString().trim();
            String description = description_input.getText().toString().trim();

            Bundle result = new Bundle();
            result.putString("title", title);
            result.putString("description", description);

            // Send result back to parent fragment
            getParentFragmentManager().setFragmentResult("metadata_request_key", result);

            dismiss(); // close bottom sheet
        });
    }

    public static EditDocumentFragment newInstance(@Nullable String title, @Nullable String description) {
        EditDocumentFragment fragment = new EditDocumentFragment();
        Bundle args = new Bundle();
        args.putString("file_title", title);
        args.putString("file_description", description);
        fragment.setArguments(args);
        return fragment;
    }

    public interface MetadataListener {
        void onMetadataEntered(String title, String description);
    }
}

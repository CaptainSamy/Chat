package com.example.chatappdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class Fragment_Dangky extends Fragment {
    private Button register_button;
    private TextInputLayout register_Email, register_Password, register_ConfirmPassword,
            register_Name, register_Phone;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private RadioGroup radioGroup;
    private RadioButton maleCheckBox, femaleCheckBox;
    private ProgressDialog loadingBar;
    private  User user;
    private DatabaseReference databaseReference;
    public String email_signup, password_signup, gioitinh_signup, rePassword_signup, name_signup, phone_signup;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z][\\w-]+@([\\w]+\\.[\\w]+|[\\w]+\\.[\\w]{2,}\\.[\\w]{2,})$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{6,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(09|01[2|6|8|9])+([0-9]{8})\\b");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_dangky, container, false);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        loadingBar = new ProgressDialog(getActivity());
        register_button = view.findViewById(R.id.register_button);
        register_Email = view.findViewById(R.id.register_email);
        register_Password = view.findViewById(R.id.register_password);
        register_Name = view.findViewById(R.id.register_name);
        register_Phone = view.findViewById(R.id.register_phone);
        maleCheckBox = view.findViewById(R.id.male_checkbox);
        femaleCheckBox = view.findViewById(R.id.female_checkbox);
        maleCheckBox.setChecked(true);
        radioGroup = view.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.male_checkbox){
                    gioitinh_signup = "Nam";
                    femaleCheckBox.setChecked(false);
                }else if (checkedId == R.id.female_checkbox){
                    gioitinh_signup = "Nữ";
                    maleCheckBox.setChecked(false);

                }
            }
        });



        register_ConfirmPassword = view.findViewById(R.id.register_confirm_password);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccout();
            }
        });
        return view;
    }




    private void CreateNewAccout() {
        email_signup = register_Email.getEditText().getText().toString();
        password_signup = register_Password.getEditText().getText().toString();
        rePassword_signup = register_ConfirmPassword.getEditText().getText().toString();
        name_signup = register_Name.getEditText().getText().toString();
        phone_signup = register_Phone.getEditText().getText().toString();
        gioitinh_signup = (femaleCheckBox.isChecked()) ? "Nam" : "Nữ";


        if (!validateEmail() | !validatePassword() | !validateRePassword() | !validateName() |  !validatePhone()) {
            return;
        } else {
            loadingBar.setTitle("Đang tạo tài khoản");
            loadingBar.setMessage("Đợi chút...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            user = new User(email_signup,password_signup,gioitinh_signup,name_signup,phone_signup);
            registerUser(email_signup,password_signup);
        }
    }

    public void registerUser(String email_signup, String password_signup){
        firebaseAuth.createUserWithEmailAndPassword(email_signup, password_signup)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String currentUserID = firebaseAuth.getCurrentUser().getUid();
                            databaseReference.child("Users").child(currentUserID).setValue("");
                            Toast.makeText(getActivity(), "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            updateUI(user);
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(getActivity(), "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }

    public  void updateUI(User currentUser){
        String keyId = databaseReference.push().getKey();
        databaseReference.child(keyId).setValue(user);
        Intent loginIntent = new Intent(getActivity(),MainActivity.class);
        startActivity(loginIntent);
    }

    private boolean validateEmail() {
        email_signup = register_Email.getEditText().getText().toString().trim();
        if (email_signup.isEmpty()) {
            register_Email.setError("Bạn không được để trống!");
            return false;
        } else if (!EMAIL_PATTERN.matcher(email_signup).matches()) {
            register_Email.setError("Vui lòng nhập đúng định dạng email!");
            return false;
        } else {
            register_Email.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        password_signup = register_Password.getEditText().getText().toString().trim();
        if (password_signup.isEmpty()) {
            register_Password.setError("Bạn không được để trống!");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(password_signup).matches()) {
            register_Password.setError("Mật khẩu cần có 1 chữ viết hoa và dài hơn 6 kí tự gồm chữ và số!");
            return false;
        } else {
            register_Password.setError(null);
            return true;
        }
    }

    private boolean validateRePassword() {
        rePassword_signup = register_ConfirmPassword.getEditText().getText().toString().trim();
        password_signup = register_Password.getEditText().getText().toString().trim();
        if (rePassword_signup.isEmpty()) {
            register_ConfirmPassword.setError("Bạn không được để trống!");
            return false;
        } else if (!rePassword_signup.equals(password_signup)) {
            register_ConfirmPassword.setError("Mật khẩu nhập lại khác mật khẩu ở trên!");
            return false;
        } else {
            register_ConfirmPassword.setError(null);
            return true;
        }
    }

    private boolean validateName() {
        name_signup = register_Name.getEditText().getText().toString().trim();
        if (name_signup.isEmpty()) {
            register_Name.setError("Bạn không được để trống!");
            return false;
        } else {
            register_Name.setError(null);
            return true;
        }
    }

    private boolean validatePhone() {
        phone_signup = register_Phone.getEditText().getText().toString().trim();
        if (phone_signup.isEmpty()) {
            register_Phone.setError("Bạn không được để trống!");
            return false;
        } else if (!PHONE_PATTERN.matcher(phone_signup).matches()) {
            register_Phone.setError("Vui lòng nhập đúng định dạng số điện thoại!");
            return false;
        } else {
            register_Phone.setError(null);
            return true;
        }
    }
}

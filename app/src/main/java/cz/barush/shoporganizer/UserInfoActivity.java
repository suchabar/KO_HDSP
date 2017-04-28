package cz.barush.shoporganizer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import cz.barush.shoporganizer.persistance.entity.User;
import cz.barush.shoporganizer.utils.StaticPool;

public class UserInfoActivity extends AppCompatActivity
{
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setLogo(R.drawable.logo);

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(UserInfoActivity.this, SelectionOfFood1Activity.class);
                startActivity(intent);
            }
        });

        setListeners();
    }

    private void setListeners()
    {
        ((Spinner)findViewById(R.id.tag_groupName)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                StaticPool.user.setActivity(User.ActivityIntensity.values()[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                return;
            }
        });

        //SET NAME
        ((EditText)findViewById(R.id.tag_name)).setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean b)
            {
                if(!b)StaticPool.user.setName(((EditText)findViewById(R.id.tag_name)).getText().toString());
            }
        });

        //SET GENDER
        ((RadioButton) findViewById(R.id.tag_woman)).setOnClickListener(MyOnRadioClickListener());
        ((RadioButton) findViewById(R.id.tag_man)).setOnClickListener(MyOnRadioClickListener());

        //SET HEIGHT
        ((EditText)findViewById(R.id.tag_height)).setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean b)
            {
                //When user leaves the EditText
                if(!b)StaticPool.user.setHeight(Integer.valueOf(((EditText)findViewById(R.id.tag_height)).getText().toString()));
            }
        });

        //SET WEIGHT
        ((EditText)findViewById(R.id.tag_weight)).setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean b)
            {
                if(!b)StaticPool.user.setWeight(Integer.valueOf(((EditText)findViewById(R.id.tag_weight)).getText().toString()));
            }
        });
    }

    private View.OnClickListener MyOnRadioClickListener()
    {
        return new View.OnClickListener()
        {
            public void onClick(View v)
            {
                boolean isWoman = ((RadioButton)findViewById(R.id.tag_woman)).isChecked();
                if (isWoman)StaticPool.user.setGender(User.Gender.WOMAN);
                else StaticPool.user.setGender(User.Gender.MAN);
            }

            private void changeIfPregnantVisibility()
            {
            }
        };
    }

}

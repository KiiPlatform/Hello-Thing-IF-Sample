/*
 * Copyright 2016 Kii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kii.sample.hellothingif;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.kii.cloud.storage.KiiUser;
import com.kii.thingif.ThingIFAPI;
import com.kii.sample.hellothingif.util.ProgressDialogFragment;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class LoginFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private Unbinder mButterknifeUnbunder;
    private AndroidDeferredManager mAdm = new AndroidDeferredManager();

    @BindView(R.id.editTextUsername) EditText mUsername;
    @BindView(R.id.editTextUserPassword) EditText mUserPassword;
    @BindView(R.id.editTextVendorThingID) EditText mVendorThingID;
    @BindView(R.id.editTextThingPassword) EditText mThingPassword;

    public LoginFragment() {
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mButterknifeUnbunder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        ProgressDialogFragment.close(getFragmentManager());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mButterknifeUnbunder.unbind();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick(R.id.buttonAddUser)
    void onAddUserClicked() {
        String username = mUsername.getText().toString();
        String userPassword = mUserPassword.getText().toString();

        ProgressDialogFragment.show(getActivity(), getFragmentManager(), R.string.progress_add_user);

        PromiseAPIWrapper api = new PromiseAPIWrapper(mAdm, null);
        mAdm.when(api.addUser(username, userPassword)
        ).then(new DoneCallback<KiiUser>() {
            @Override
            public void onDone(KiiUser user) {
                ProgressDialogFragment.close(getFragmentManager());
                showToast("The user has been added.");
            }
        }).fail(new FailCallback<Throwable>() {
            @Override
            public void onFail(final Throwable tr) {
                ProgressDialogFragment.close(getFragmentManager());
                showToast("Failed to add the user: " + tr.getLocalizedMessage());
            }
        });
    }

    @OnClick(R.id.buttonOnboard)
    void onOnboardClicked() {
        String username = mUsername.getText().toString();
        String userPassword = mUserPassword.getText().toString();
        String vendorThingID = mVendorThingID.getText().toString();
        String thingPassword = mThingPassword.getText().toString();

        ProgressDialogFragment.show(getActivity(), getFragmentManager(), R.string.progress_onboard);

        PromiseAPIWrapper api = new PromiseAPIWrapper(mAdm, null);
        mAdm.when(api.initializeThingIFAPI(getContext(), username, userPassword, vendorThingID, thingPassword)
        ).then(new DoneCallback<ThingIFAPI>() {
            @Override
            public void onDone(ThingIFAPI api) {
                if (getActivity() == null) {
                    return;
                }
                ProgressDialogFragment.close(getFragmentManager());
                if (mListener != null) {
                    mListener.onThingIFInitialized(api);
                }
            }
        }).fail(new FailCallback<Throwable>() {
            @Override
            public void onFail(final Throwable tr) {
                ProgressDialogFragment.close(getFragmentManager());
                showToast("Failed to onboard the thing: " + tr.getLocalizedMessage());
            }
        });
    }

    private void showToast(String message) {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    public interface OnFragmentInteractionListener {
        void onThingIFInitialized(ThingIFAPI api);
    }
}

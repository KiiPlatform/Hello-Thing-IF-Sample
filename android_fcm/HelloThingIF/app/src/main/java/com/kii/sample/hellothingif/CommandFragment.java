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

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.sample.hellothingif.command.LEDState;
import com.kii.sample.hellothingif.command.SetBrightness;
import com.kii.sample.hellothingif.command.TurnPower;
import com.kii.sample.hellothingif.fcm.MyFirebaseMessagingService;
import com.kii.thingif.ThingIFAPI;
import com.kii.thingif.command.Action;
import com.kii.thingif.command.ActionResult;
import com.kii.thingif.command.Command;
import com.kii.sample.hellothingif.util.ProgressDialogFragment;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class CommandFragment extends Fragment {
    private static final String ARG_THING_IF_API = "ThingIFAPI";
    private static final String BUNDLE_KEY_STATE_POWER = "BundleKeyStatePower";
    private static final String BUNDLE_KEY_STATE_BRIGHTNESS = "BundleKeyStateBrightness";
    private static final String BUNDLE_KEY_STATE_MOTION = "BundleKeyStateMotion";

    private Unbinder mButterknifeUnbunder;
    private AndroidDeferredManager mAdm = new AndroidDeferredManager();
    private ThingIFAPI mApi;

    @BindView(R.id.checkBoxPowerOn) CheckBox mCheckBoxPowerOn;
    @BindView(R.id.textViewBrightness) TextView mTextViewBrightness;
    @BindView(R.id.seekBarBrightness) SeekBar mSeekBarBrightness;
    @BindView(R.id.textViewCommandResult) TextView mCommandResult;
    @BindView(R.id.textViewStatePower) TextView mTextViewStatePower;
    @BindView(R.id.textViewStateBrightness) TextView mTextViewStateBrightness;
    @BindView(R.id.textViewMotion) TextView mTextViewStateMotion;

    public CommandFragment() {
    }

    public static CommandFragment newInstance(ThingIFAPI api) {
        CommandFragment fragment = new CommandFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_THING_IF_API, api);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mApi = getArguments().getParcelable(ARG_THING_IF_API);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_command, container, false);
        mButterknifeUnbunder = ButterKnife.bind(this, view);

        mSeekBarBrightness.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        if (savedInstanceState == null) {
            mCheckBoxPowerOn.setChecked(true);
            mSeekBarBrightness.setProgress(SetBrightness.MAX_BRIGHTNESS_VALUE);
        } else {
            mTextViewStatePower.setText(savedInstanceState.getString(BUNDLE_KEY_STATE_POWER, ""));
            mTextViewStateBrightness.setText(savedInstanceState.getString(BUNDLE_KEY_STATE_BRIGHTNESS, ""));
            mTextViewStateMotion.setText(savedInstanceState.getString(BUNDLE_KEY_STATE_MOTION, ""));
        }

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        ProgressDialogFragment.close(getFragmentManager());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_KEY_STATE_POWER, mTextViewStatePower.getText().toString());
        outState.putString(BUNDLE_KEY_STATE_BRIGHTNESS, mTextViewStateBrightness.getText().toString());
        outState.putString(BUNDLE_KEY_STATE_MOTION, mTextViewStateMotion.getText().toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mCommandResultBroadcastReceiver,
                new IntentFilter(MyFirebaseMessagingService.INTENT_COMMAND_RESULT_RECEIVED));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mCommandResultBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mButterknifeUnbunder.unbind();
    }

    @OnClick(R.id.buttonSend)
    void onSendCommand() {
        ProgressDialogFragment.show(getActivity(), getFragmentManager(), R.string.progress_send_command);
        mCommandResult.setText("");

        List<Action> actions = new ArrayList<>();

        TurnPower action1 = new TurnPower();
        action1.power = mCheckBoxPowerOn.isChecked();
        actions.add(action1);

        if (action1.power) {
            SetBrightness action2 = new SetBrightness();
            action2.brightness = mSeekBarBrightness.getProgress();
            actions.add(action2);
        }

        PromiseAPIWrapper api = new PromiseAPIWrapper(mAdm, mApi);
        mAdm.when(api.postNewCommand(HelloThingIF.SCHEMA_NAME, HelloThingIF.SCHEMA_VERSION, actions)
        ).then(new DoneCallback<Command>() {
            @Override
            public void onDone(Command command) {
                ProgressDialogFragment.close(getFragmentManager());
                String commandID = command.getCommandID();
                showToast("The command has been sent: " + commandID);
            }
        }).fail(new FailCallback<Throwable>() {
            @Override
            public void onFail(final Throwable tr) {
                ProgressDialogFragment.close(getFragmentManager());
                showToast("Failed to send the command: " + tr.getLocalizedMessage());
            }
        });
    }

    @OnClick(R.id.buttonRefresh)
    void onRefreshState() {
        ProgressDialogFragment.show(getActivity(), getFragmentManager(), R.string.progress_receive_state);

        PromiseAPIWrapper api = new PromiseAPIWrapper(mAdm, mApi);
        mAdm.when(api.getTargetState()
        ).then(new DoneCallback<LEDState>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDone(LEDState state) {
                if (getActivity() == null) {
                    return;
                }
                ProgressDialogFragment.close(getFragmentManager());
                if (state.power) {
                    mTextViewStatePower.setText("Power: ON");
                } else {
                    mTextViewStatePower.setText("Power: OFF");
                }
                mTextViewStateBrightness.setText("Brightness: " + state.brightness);
                mTextViewStateMotion.setText("Motion: " + state.motion);
                showToast("The state has been refreshed.");
            }
        }).fail(new FailCallback<Throwable>() {
            @Override
            public void onFail(final Throwable tr) {
                ProgressDialogFragment.close(getFragmentManager());
                showToast("Failed to receive the state: " + tr.getLocalizedMessage());
            }
        });
    }

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mTextViewBrightness.setText("Brightness: " + progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private BroadcastReceiver mCommandResultBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String commandID = intent.getStringExtra(MyFirebaseMessagingService.PARAM_COMMAND_ID);
            onPushMessageReceived(commandID);
        }
    };

    private void onPushMessageReceived(String commandID) {
        PromiseAPIWrapper api = new PromiseAPIWrapper(mAdm, mApi);
        mAdm.when(api.getCommand(commandID)
        ).then(new DoneCallback<Command>() {
            @SuppressWarnings("unused")
            @Override
            public void onDone(Command command) {
                if (getActivity() == null) {
                    return;
                }
                List<ActionResult> results = command.getActionResults();
                StringBuilder sbMessage = new StringBuilder();
                for (ActionResult result : results) {
                    String actionName = result.getActionName();
                    boolean succeeded = result.succeeded();
                    String errorMessage = result.getErrorMessage();
                    if (!succeeded) {
                        sbMessage.append(errorMessage);
                    }
                }
                if (sbMessage.length() == 0) {
                    sbMessage.append("The command succeeded.");
                }
                mCommandResult.setText(sbMessage.toString());
            }
        }).fail(new FailCallback<Throwable>() {
            @Override
            public void onFail(final Throwable tr) {
                showToast("Failed to receive the command result: " + tr.getLocalizedMessage());
            }
        });
    }

    private void showToast(String message) {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}

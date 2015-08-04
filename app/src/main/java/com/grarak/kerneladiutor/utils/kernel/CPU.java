/*
 * Copyright (C) 2015 Willi Ye
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.grarak.kerneladiutor.utils.kernel;

import android.content.Context;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.root.Control;
import com.grarak.kerneladiutor.utils.root.LinuxUtils;
import com.grarak.kerneladiutor.utils.root.RootUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by willi on 02.12.14.
 */
public class CPU implements Constants {

    private static int cores;
    private static int bigCore = -1;
    private static int LITTLEcore = -1;
    private static Integer[][] mFreqs;
    private static String[][] mAvailableGovernors;
    private static String[] mMcPowerSavingItems;
    private static String[] mAvailableCFSSchedulers;

    private static String TEMP_FILE;

    private static String[] mCpuQuietAvailableGovernors;

    private static String CPU_BOOST_ENABLE_FILE;

    public static void setCpuBoostInputMs(int value, Context context) {
        Control.runCommand(String.valueOf(value), CPU_BOOST_INPUT_MS, Control.CommandType.GENERIC, context);
    }

    public static int getCpuBootInputMs() {
        return Utils.stringToInt(Utils.readFile(CPU_BOOST_INPUT_MS));
    }

    public static boolean hasCpuBoostInputMs() {
        return Utils.existFile(CPU_BOOST_INPUT_MS);
    }

    public static void setZaneZamProfile(int value, Context context) {
        Control.runCommand(value == 0 ? "0" : getZaneZamProfiles(context).get(value),
                CPU_ZANEZAM_PROFILE, Control.CommandType.GENERIC, context);
    }

    public static String getCurZaneZamProfile() {
        return Utils.readFile(CPU_ZANEZAM_PROFILE);
    }

    public static List<String> getZaneZamProfiles(Context context) {
        List<String> list = new ArrayList<>();
        list.add(context.getString(R.string.none));
        list.add("Default");
        list.add("Yank Battery");
        list.add("Yank Battery Extreme");
        list.add("ZaneZam Battery");
        list.add("ZaneZam Battery Plus");
        list.add("ZaneZam Optimized");
        list.add("ZaneZam Moderate");
        list.add("ZaneZam Performance");
        list.add("ZaneZam InZane");
        list.add("ZaneZam Gaming");
        return list;
    }

    public static boolean hasZaneZamProfile() {
        return Utils.existFile(CPU_ZANEZAM_PROFILE);
    }

    public static void setCpuBoostInputFreq(int value, int core, Context context) {
        String freqs;
        if ((freqs = Utils.readFile(CPU_BOOST_INPUT_BOOST_FREQ)).contains(":")) {
            StringBuilder command = new StringBuilder();
            for (String freq : freqs.split(" "))
                if (freq.startsWith(core + ":"))
                    command.append(core).append(":").append(value).append(" ");
                else command.append(freq).append(" ");
            command.setLength(command.length() - 1);
            Control.runCommand(command.toString(), CPU_BOOST_INPUT_BOOST_FREQ, Control.CommandType.GENERIC, context);
        } else
            Control.runCommand(String.valueOf(value), CPU_BOOST_INPUT_BOOST_FREQ, Control.CommandType.GENERIC, context);
    }

    public static List<Integer> getCpuBootInputFreq() {
        List<Integer> list = new ArrayList<>();
        String value = Utils.readFile(CPU_BOOST_INPUT_BOOST_FREQ);
        for (String core : value.split(" ")) {
            if (core.contains(":")) core = core.split(":")[1];
            if (core.equals("0")) list.add(0);
            else list.add(CPU.getFreqs().indexOf(Utils.stringToInt(core)) + 1);
        }
        return list;
    }

    public static boolean hasCpuBoostInputFreq() {
        return Utils.existFile(CPU_BOOST_INPUT_BOOST_FREQ);
    }

    public static void setCpuBoostSyncThreshold(int value, Context context) {
        Control.runCommand(String.valueOf(value), CPU_BOOST_SYNC_THRESHOLD, Control.CommandType.GENERIC, context);
    }

    public static int getCpuBootSyncThreshold() {
        String value = Utils.readFile(CPU_BOOST_SYNC_THRESHOLD);
        if (value.equals("0")) return 0;
        return CPU.getFreqs().indexOf(Utils.stringToInt(value)) + 1;
    }

    public static boolean hasCpuBoostSyncThreshold() {
        return Utils.existFile(CPU_BOOST_SYNC_THRESHOLD);
    }

    public static void setCpuBoostMs(int value, Context context) {
        Control.runCommand(String.valueOf(value), CPU_BOOST_MS, Control.CommandType.GENERIC, context);
    }

    public static int getCpuBootMs() {
        return Utils.stringToInt(Utils.readFile(CPU_BOOST_MS));
    }

    public static boolean hasCpuBoostMs() {
        return Utils.existFile(CPU_BOOST_MS);
    }

    public static void activateCpuBoostDebugMask(boolean active, Context context) {
        Control.runCommand(active ? "1" : "0", CPU_BOOST_DEBUG_MASK, Control.CommandType.GENERIC, context);
    }

    public static boolean isCpuBoostDebugMaskActive() {
        return Utils.readFile(CPU_BOOST_DEBUG_MASK).equals("1");
    }

    public static boolean hasCpuBoostDebugMask() {
        return Utils.existFile(CPU_BOOST_DEBUG_MASK);
    }

    public static void activateCpuBoost(boolean active, Context context) {
        String command = active ? "1" : "0";
        if (CPU_BOOST_ENABLE_FILE.equals(CPU_BOOST_ENABLE_2)) command = active ? "Y" : "N";
        Control.runCommand(command, CPU_BOOST_ENABLE_FILE, Control.CommandType.GENERIC, context);
    }

    public static boolean isCpuBoostActive() {
        String value = Utils.readFile(CPU_BOOST_ENABLE_FILE);
        return value.equals("1") || value.equals("Y");
    }

    public static boolean hasCpuBoostEnable() {
        if (Utils.existFile(CPU_BOOST_ENABLE)) CPU_BOOST_ENABLE_FILE = CPU_BOOST_ENABLE;
        else if (Utils.existFile(CPU_BOOST_ENABLE_2)) CPU_BOOST_ENABLE_FILE = CPU_BOOST_ENABLE_2;
        return CPU_BOOST_ENABLE_FILE != null;
    }

    public static boolean hasCpuBoost() {
        return Utils.existFile(CPU_BOOST);
    }

    public static void setCpuQuietGovernor(String value, Context context) {
        Control.runCommand(value, CPU_QUIET_CURRENT_GOVERNOR, Control.CommandType.GENERIC, context);
    }

    public static String getCpuQuietCurGovernor() {
        return Utils.readFile(CPU_QUIET_CURRENT_GOVERNOR);
    }

    public static List<String> getCpuQuietAvailableGovernors() {
        if (mCpuQuietAvailableGovernors == null) {
            String[] governors = Utils.readFile(CPU_QUIET_AVAILABLE_GOVERNORS).split(" ");
            if (governors.length > 0) {
                mCpuQuietAvailableGovernors = new String[governors.length];
                System.arraycopy(governors, 0, mCpuQuietAvailableGovernors, 0, mCpuQuietAvailableGovernors.length);
            }
        }
        if (mCpuQuietAvailableGovernors == null) return null;
        return new ArrayList<>(Arrays.asList(mCpuQuietAvailableGovernors));
    }

    public static boolean hasCpuQuietGovernors() {
        return Utils.existFile(CPU_QUIET_AVAILABLE_GOVERNORS) && Utils.existFile(CPU_QUIET_CURRENT_GOVERNOR)
                && !Utils.readFile(CPU_QUIET_AVAILABLE_GOVERNORS).equals("none");
    }

    public static void activateCpuQuiet(boolean active, Context context) {
        Control.runCommand(active ? "1" : "0", CPU_QUIET_ENABLE, Control.CommandType.GENERIC, context);
    }

    public static boolean isCpuQuietActive() {
        return Utils.readFile(CPU_QUIET_ENABLE).equals("1");
    }

    public static boolean hasCpuQuietEnable() {
        return Utils.existFile(CPU_QUIET_ENABLE);
    }

    public static boolean hasCpuQuiet() {
        return Utils.existFile(CPU_QUIET);
    }

    public static void setCFSScheduler(String value, Context context) {
        Control.runCommand(value, CPU_CURRENT_CFS_SCHEDULER, Control.CommandType.GENERIC, context);
    }

    public static String getCurrentCFSScheduler() {
        return Utils.readFile(CPU_CURRENT_CFS_SCHEDULER);
    }

    public static List<String> getAvailableCFSSchedulers() {
        if (mAvailableCFSSchedulers == null)
            mAvailableCFSSchedulers = Utils.readFile(CPU_AVAILABLE_CFS_SCHEDULERS).split(" ");
        return new ArrayList<>(Arrays.asList(mAvailableCFSSchedulers));
    }

    public static boolean hasCFSScheduler() {
        return Utils.existFile(CPU_AVAILABLE_CFS_SCHEDULERS) && Utils.existFile(CPU_CURRENT_CFS_SCHEDULER);
    }

    public static String[] getMcPowerSavingItems(Context context) {
        if (mMcPowerSavingItems == null && context != null)
            mMcPowerSavingItems = context.getResources().getStringArray(R.array.mc_power_saving_items);
        return mMcPowerSavingItems;
    }

    public static void setMcPowerSaving(int value, Context context) {
        Control.runCommand(String.valueOf(value), CPU_MC_POWER_SAVING, Control.CommandType.GENERIC, context);
    }

    public static int getCurMcPowerSaving() {
        return Utils.stringToInt(Utils.readFile(CPU_MC_POWER_SAVING));
    }

    public static boolean hasMcPowerSaving() {
        return Utils.existFile(CPU_MC_POWER_SAVING);
    }

    public static void activatePowerSavingWq(boolean active, Context context) {
        String command = active ? "Y" : "N";
        Control.runCommand(command, CPU_WQ_POWER_SAVING, Control.CommandType.GENERIC, context);
    }

    public static boolean isPowerSavingWqActive() {
        String value = Utils.readFile(CPU_WQ_POWER_SAVING);
        return value.equals("Y");
    }

    public static boolean hasPowerSavingWq() {
        return Utils.existFile(CPU_WQ_POWER_SAVING);
    }

    public static List<String> getAvailableGovernors() {
        return getAvailableGovernors(getBigCore());
    }

    public static List<String> getAvailableGovernors(int core) {
        if (mAvailableGovernors == null) mAvailableGovernors = new String[getCoreCount()][];
        if (mAvailableGovernors[core] == null) {
            String value = Utils.readFile(CPU_AVAILABLE_GOVERNORS);
            if (value != null) mAvailableGovernors[core] = value.split(" ");
        }
        if (mAvailableGovernors[core] == null) return null;
        return new ArrayList<>(Arrays.asList(mAvailableGovernors[core]));
    }

    public static void setGovernor(String governor, Context context) {
        setGovernor(Control.CommandType.CPU, governor, context);
    }

    public static void setGovernor(Control.CommandType command, String governor, Context context) {
        Control.runCommand(governor, CPU_SCALING_GOVERNOR, command, context);
    }

    public static String getCurGovernor(boolean forceRead) {
        return getCurGovernor(getBigCore(), forceRead);
    }

    public static String getCurGovernor(int core, boolean forceRead) {
        if (forceRead && core > 0)
            while (!Utils.existFile(String.format(CPU_SCALING_GOVERNOR, core)))
                activateCore(core, true, null);
        if (Utils.existFile(String.format(CPU_SCALING_GOVERNOR, core))) {
            String value = Utils.readFile(String.format(CPU_SCALING_GOVERNOR, core));
            if (value != null) return value;
        }
        return "";
    }

    public static List<Integer> getFreqs() {
        return getFreqs(getBigCore());
    }

    public static List<Integer> getFreqs(int core) {
        if (mFreqs == null) mFreqs = new Integer[getCoreCount()][];
        if (mFreqs[core] == null)
            if (Utils.existFile(String.format(CPU_AVAILABLE_FREQS, 0))) {
                if (core > 0) while (!Utils.existFile(String.format(CPU_AVAILABLE_FREQS, core)))
                    activateCore(core, true, null);
                String values;
                if ((values = Utils.readFile(String.format(CPU_AVAILABLE_FREQS, core))) != null) {
                    String[] valueArray = values.split(" ");
                    mFreqs[core] = new Integer[valueArray.length];
                    for (int i = 0; i < mFreqs[core].length; i++)
                        mFreqs[core][i] = Utils.stringToInt(valueArray[i]);
                }
            } else if (Utils.existFile(String.format(CPU_TIME_STATE, 0))) {
                if (core > 0) while (!Utils.existFile(String.format(CPU_TIME_STATE, core)))
                    activateCore(core, true, null);
                String values;
                if ((values = Utils.readFile(String.format(CPU_TIME_STATE, core))) != null) {
                    String[] valueArray = values.split("\\r?\\n");
                    mFreqs[core] = new Integer[valueArray.length];
                    for (int i = 0; i < mFreqs[core].length; i++)
                        mFreqs[core][i] = Utils.stringToInt(valueArray[i].split(" ")[0]);
                }
            }
        if (mFreqs[core] == null) return null;
        List<Integer> freqs = Arrays.asList(mFreqs[core]);
        Collections.sort(freqs);
        return freqs;
    }

    public static void setMaxScreenOffFreq(int freq, Context context) {
        setMaxScreenOffFreq(Control.CommandType.CPU, freq, context);
    }

    public static void setMaxScreenOffFreq(Control.CommandType command, int freq, Context context) {
        Control.runCommand(String.valueOf(freq), CPU_MAX_SCREEN_OFF_FREQ, command, context);
    }

    public static int getMaxScreenOffFreq(boolean forceRead) {
        return getMaxScreenOffFreq(getBigCore(), forceRead);
    }

    public static int getMaxScreenOffFreq(int core, boolean forceRead) {
        if (forceRead && core > 0)
            while (!Utils.existFile(String.format(CPU_MAX_SCREEN_OFF_FREQ, core)))
                activateCore(core, true, null);
        if (Utils.existFile(String.format(CPU_MAX_SCREEN_OFF_FREQ, core))) {
            String value = Utils.readFile(String.format(CPU_MAX_SCREEN_OFF_FREQ, core));
            if (value != null) return Utils.stringToInt(value);
        }
        return 0;
    }

    public static boolean hasMaxScreenOffFreq() {
        return Utils.existFile(String.format(CPU_MAX_SCREEN_OFF_FREQ, 0));
    }

    public static void setMinFreq(int freq, Context context) {
        setMinFreq(Control.CommandType.CPU, freq, context);
    }

    public static void setMinFreq(Control.CommandType command, int freq, Context context) {
        if (getMaxFreq(command == Control.CommandType.CPU ? getBigCore() : getLITTLEcore(), true) < freq)
            setMaxFreq(command, freq, context);
        Control.runCommand(String.valueOf(freq), CPU_MIN_FREQ, command, context);
    }

    public static int getMinFreq(boolean forceRead) {
        return getMinFreq(getBigCore(), forceRead);
    }

    public static int getMinFreq(int core, boolean forceRead) {
        if (forceRead && core > 0) while (!Utils.existFile(String.format(CPU_MIN_FREQ, core)))
            activateCore(core, true, null);
        if (Utils.existFile(String.format(CPU_MIN_FREQ, core))) {
            String value = Utils.readFile(String.format(CPU_MIN_FREQ, core));
            if (value != null) return Utils.stringToInt(value);
        }
        return 0;
    }

    public static void setMaxFreq(int freq, Context context) {
        setMaxFreq(Control.CommandType.CPU, freq, context);
    }

    public static void setMaxFreq(Control.CommandType command, int freq, Context context) {
        if (command == Control.CommandType.CPU && Utils.existFile(CPU_MSM_CPUFREQ_LIMIT)
                && freq > Utils.stringToInt(Utils.readFile(CPU_MSM_CPUFREQ_LIMIT)))
            Control.runCommand(String.valueOf(freq), CPU_MSM_CPUFREQ_LIMIT, Control.CommandType.GENERIC, context);
        if (Utils.existFile(String.format(CPU_ENABLE_OC, 0)))
            Control.runCommand("1", CPU_ENABLE_OC, Control.CommandType.CPU, context);
        if (getMinFreq(command == Control.CommandType.CPU ? getBigCore() : getLITTLEcore(), true) > freq)
            setMinFreq(command, freq, context);
        if (Utils.existFile(String.format(CPU_MAX_FREQ_KT, 0)))
            Control.runCommand(String.valueOf(freq), CPU_MAX_FREQ_KT, command, context);
        else Control.runCommand(String.valueOf(freq), CPU_MAX_FREQ, command, context);
    }

    public static int getMaxFreq(boolean forceRead) {
        return getMaxFreq(getBigCore(), forceRead);
    }

    public static int getMaxFreq(int core, boolean forceRead) {
        if (forceRead && core > 0) while (!Utils.existFile(String.format(CPU_MAX_FREQ, core)))
            activateCore(core, true, null);
        if (forceRead && core > 0 && Utils.existFile(String.format(CPU_MAX_FREQ_KT, 0)))
            while (!Utils.existFile(String.format(CPU_MAX_FREQ_KT, core)))
                activateCore(core, true, null);

        if (Utils.existFile(String.format(CPU_MAX_FREQ_KT, core))) {
            String value = Utils.readFile(String.format(CPU_MAX_FREQ_KT, core));
            if (value != null) return Utils.stringToInt(value);
        }
        if (Utils.existFile(String.format(CPU_MAX_FREQ, core))) {
            String value = Utils.readFile(String.format(CPU_MAX_FREQ, core));
            if (value != null) return Utils.stringToInt(value);
        }
        return 0;
    }

    public static int getCurFreq(int core) {
        if (Utils.existFile(String.format(CPU_CUR_FREQ, core))) {
            String value = Utils.readFile(String.format(CPU_CUR_FREQ, core));
            if (value != null) return Utils.stringToInt(value);
        }
        return 0;
    }

    public static void onlineAllCores(Context context) {
        for (int i = 1; i < getCoreCount(); i++) activateCore(i, true, context);
    }

    public static void activateCore(int core, boolean active, Context context) {
        if (context != null)
            Control.runCommand(active ? "1" : "0", String.format(CPU_CORE_ONLINE, core), Control.CommandType.GENERIC, context);
        else
            RootUtils.runCommand(String.format("echo %s > " + String.format(CPU_CORE_ONLINE, core), active ? "1" : "0"));
    }

    public static List<Integer> getLITTLECoreRange() {
        List<Integer> list = new ArrayList<>();
        if (!isBigLITTLE()) for (int i = 0; i < getCoreCount(); i++) list.add(i);
        else if (getLITTLEcore() == 0) for (int i = 0; i < 4; i++) list.add(i);
        else for (int i = getLITTLEcore(); i < getCoreCount(); i++) list.add(i);
        return list;
    }

    public static List<Integer> getBigCoreRange() {
        List<Integer> list = new ArrayList<>();
        if (!isBigLITTLE()) for (int i = 0; i < getCoreCount(); i++) list.add(i);
        else if (getBigCore() == 0) for (int i = 0; i < 4; i++) list.add(i);
        else for (int i = getBigCore(); i < getCoreCount(); i++) list.add(i);
        return list;
    }

    public static int getLITTLEcore() {
        isBigLITTLE();
        return LITTLEcore == -1 ? 0 : LITTLEcore;
    }

    public static int getBigCore() {
        isBigLITTLE();
        return bigCore == -1 ? 0 : bigCore;
    }

    public static boolean isBigLITTLE() {
        boolean bigLITTLE = getCoreCount() > 4;
        if (!bigLITTLE) return false;

        if (bigCore == -1 || LITTLEcore == -1) {
            List<Integer> cpu0Freqs = getFreqs(0);
            List<Integer> cpu4Freqs = getFreqs(4);
            if (cpu0Freqs != null && cpu4Freqs != null) {
                if (cpu0Freqs.size() > cpu4Freqs.size()) {
                    bigCore = 0;
                    LITTLEcore = 4;
                } else {
                    bigCore = 4;
                    LITTLEcore = 0;
                }
            }
        }

        return bigCore != -1 && LITTLEcore != -1;
    }

    public static int getCoreCount() {
        return cores == 0 ? cores = Runtime.getRuntime().availableProcessors() : cores;
    }

    public static String getTemp() {
        double temp = Utils.stringToLong(Utils.readFile(TEMP_FILE));
        if (temp > 1000) temp /= 1000;
        else if (temp > 200) temp /= 10;
        return Utils.formatCelsius(temp) + " " + Utils.celsiusToFahrenheit(temp);
    }

    public static boolean hasTemp() {
        if (Utils.existFile(CPU_TEMP_ZONE1)) {
            int temp = Utils.stringToInt(Utils.readFile(CPU_TEMP_ZONE1));
            if (temp > -1 && temp < 1000000) {
                TEMP_FILE = CPU_TEMP_ZONE1;
                return true;
            }
        }
        if (Utils.existFile(CPU_TEMP_ZONE0)) TEMP_FILE = CPU_TEMP_ZONE0;
        return TEMP_FILE != null;
    }

    /**
     * This code is from http://stackoverflow.com/a/13342738
     */
    private static LinuxUtils linuxUtils;

    public static float getCpuUsage() {
        if (linuxUtils == null) linuxUtils = new LinuxUtils();

        try {
            String cpuStat1 = linuxUtils.readSystemStat();
            Thread.sleep(1000);
            String cpuStat2 = linuxUtils.readSystemStat();
            float usage = linuxUtils.getSystemCpuUsage(cpuStat1, cpuStat2);
            if (usage > -1) return usage;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

}

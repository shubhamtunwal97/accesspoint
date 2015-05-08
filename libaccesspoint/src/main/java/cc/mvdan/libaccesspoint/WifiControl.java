/**
 * Copyright 2015 Daniel Martí
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

package cc.mvdan.libaccesspoint;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;

public class WifiControl {

	private static Method getWifiApConfiguration;
	private static Method getWifiApState;
	private static Method isWifiApEnabled;
	private static Method setWifiApEnabled;

	static {
		for (Method method : WifiManager.class.getDeclaredMethods()) {
			String name = method.getName();
			if (name.equals("getWifiApConfiguration")) {
				getWifiApConfiguration = method;
			} else if (name.equals("getWifiApState")) {
				getWifiApState = method;
			} else if (name.equals("isWifiApEnabled")) {
				isWifiApEnabled = method;
			} else if (name.equals("setWifiApEnabled")) {
				setWifiApEnabled = method;
			}
		}
	}

	public static boolean isSoftwareSupported() {
		return (getWifiApState != null
				&& isWifiApEnabled != null
				&& setWifiApEnabled != null
				&& getWifiApConfiguration != null);
	}

	public static boolean isHardwareSupported() {
		// TODO: implement via native code
		return true;
	}

	public static boolean isSupported() {
		return isSoftwareSupported() && isHardwareSupported();
	}

	private final WifiManager wm;

	private WifiControl(WifiManager wm) {
		this.wm = wm;
	}

	public static WifiControl getApControl(WifiManager wm) {
		if (!isSupported()) {
			return null;
		}
		return new WifiControl(wm);
	}

	public boolean isWifiApEnabled() {
		try {
			return (Boolean) isWifiApEnabled.invoke(wm);
		} catch (Exception e) {
			// Ignored, we just return a zero value
		}
		return false;
	}

	public static int newStateNumber(int state) {
		// WifiManager's state constants were changed around Android 4.0
		if (state < 10) {
			return state + 10;
		}
		return state;
	}

	public int getWifiApState() {
		try {
			return newStateNumber((Integer) getWifiApState.invoke(wm));
		} catch (Exception e) {
			// Ignored, we just return a zero value
		}
		return -1;
	}

	public WifiConfiguration getWifiApConfiguration() {
		try {
			return (WifiConfiguration) getWifiApConfiguration.invoke(wm);
		} catch (Exception e) {
			// Ignored, we just return a zero value
		}
		return null;
	}

	public boolean setWifiApEnabled(WifiConfiguration config, boolean enabled) {
		try {
			return (Boolean) setWifiApEnabled.invoke(wm, config, enabled);
		} catch (Exception e) {
			// Ignored, we just return a zero value
		}
		return false;
	}

	public boolean enable() {
		return setWifiApEnabled(getWifiApConfiguration(), true);
	}

	public boolean disable() {
		return setWifiApEnabled(null, false);
	}
}
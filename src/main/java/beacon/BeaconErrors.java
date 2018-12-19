package beacon;

import common.CommonUtils;

public class BeaconErrors {
    public static String toString(int error, int arg, int arg2, String text) {
        switch (error) {
            case 0: {
                return "DEBUG: " + text;
            }
            case 1: {
                return "Failed to get token";
            }
            case 2: {
                return "BypassUAC is for Windows 7 and later";
            }
            case 3: {
                return "You're already an admin";
            }
            case 4: {
                return "could not connect to pipe";
            }
            case 5: {
                return "Maximum links reached. Disconnect one";
            }
            case 6: {
                return "I'm already in SMB mode";
            }
            case 7: {
                return "could not run command (w/ token) because of its length of " + arg + " bytes!";
            }
            case 8: {
                return "could not upload file: " + arg;
            }
            case 9: {
                return "could not get file time: " + arg;
            }
            case 10: {
                return "could not set file time: " + arg;
            }
            case 11: {
                return "Could not create service: " + arg;
            }
            case 12: {
                return "Failed to impersonate token: " + arg;
            }
            case 13: {
                return "copy failed: " + arg;
            }
            case 14: {
                return "move failed: " + arg;
            }
            case 15: {
                return "ppid " + arg + " is in a different desktop session (spawned jobs may fail). Use 'ppid' to reset.";
            }
            case 16: {
                return "could not write to process memory: " + arg;
            }
            case 17: {
                return "could not adjust permissions in process: " + arg;
            }
            case 18: {
                return arg + " is an x64 process (can't inject x86 content)";
            }
            case 19: {
                return arg + " is an x86 process (can't inject x64 content)";
            }
            case 20: {
                return "Could not connect to pipe: " + arg;
            }
            case 21: {
                return "Could not bind to " + arg;
            }
            case 22: {
                return "Command length (" + arg + ") too long";
            }
            case 23: {
                return "could not create pipe: " + arg;
            }
            case 24: {
                return "Could not create token: " + arg;
            }
            case 25: {
                return "Failed to impersonate token: " + arg;
            }
            case 26: {
                return "Could not start service: " + arg;
            }
            case 27: {
                return "Could not set PPID to " + arg;
            }
            case 28: {
                return "kerberos ticket purge failed: " + arg;
            }
            case 29: {
                return "kerberos ticket use failed: " + arg;
            }
            case 30: {
                return "Could not open process token: " + arg + " (" + arg2 + ")";
            }
            case 31: {
                return "could not allocate " + arg + " bytes in process: " + arg2;
            }
            case 32: {
                return "could not create remote thread in " + arg + ": " + arg2;
            }
            case 33: {
                return "could not open process " + arg + ": " + arg2;
            }
            case 34: {
                return "Could not set PPID to " + arg + ": " + arg2;
            }
            case 35: {
                return "Could not kill " + arg + ": " + arg2;
            }
            case 36: {
                return "Could not open process token: " + arg + " (" + arg2 + ")";
            }
            case 37: {
                return "Failed to impersonate token from " + arg + " (" + arg2 + ")";
            }
            case 38: {
                return "Failed to duplicate primary token for " + arg + " (" + arg2 + ")";
            }
            case 39: {
                return "Failed to impersonate logged on user " + arg + " (" + arg2 + ")";
            }
            case 40: {
                return "Could not open '" + text + "'";
            }
            case 41: {
                return "could not spawn " + text + " (token): " + arg;
            }
            case 48: {
                return "could not spawn " + text + ": " + arg;
            }
            case 49: {
                return "could not open " + text + ": " + arg;
            }
            case 50: {
                return "Could not connect to pipe (" + text + "): " + arg;
            }
            case 51: {
                return "Could not open service control manager on " + text + ": " + arg;
            }
            case 52: {
                return "could not open " + text + ": " + arg;
            }
            case 53: {
                return "could not run " + text;
            }
            case 54: {
                return "Could not create service " + text;
            }
            case 55: {
                return "Could not start service " + text;
            }
            case 56: {
                return "Could not query service " + text;
            }
            case 57: {
                return "Could not delete service " + text;
            }
            case 58: {
                return "Privilege '" + text + "' does not exist";
            }
            case 59: {
                return "Could not open process token";
            }
            case 60: {
                return "File '" + text + "' is either too large (>4GB) or size check failed";
            }
            case 61: {
                return "Could not determine full path of '" + text + "'";
            }
            case 62: {
                return "Can only LoadLibrary() in same-arch process";
            }
            case 63: {
                return "Could not open registry key: " + arg;
            }
        }
        CommonUtils.print_error("Unknown error toString(" + error + ", " + arg + ", " + arg2 + ", '" + text + "') BEACON_ERROR");
        return "Unknown error: " + error;
    }
}


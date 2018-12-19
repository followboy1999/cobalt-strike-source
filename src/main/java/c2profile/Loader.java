package c2profile;

import common.CommonUtils;
import sleep.error.SyntaxError;
import sleep.error.YourCodeSucksException;
import sleep.parser.*;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

public class Loader {
    protected String code;
    protected Profile result;
    protected String loadme;
    protected ProfileParser parser;
    protected HashSet<String> options = new HashSet<>();
    protected HashSet<String> indicators = new HashSet<>();
    protected HashSet<String> statementa = new HashSet<>();
    protected HashSet<String> blocks = new HashSet<>();
    protected HashSet<String> statementb = new HashSet<>();
    protected HashSet<String> sealme = new HashSet<>();
    protected HashSet<String> numbers = new HashSet<>();
    protected HashSet<String> files = new HashSet<>();
    protected HashSet<String> booleans = new HashSet<>();
    protected HashSet<String> verbs = new HashSet<>();
    protected HashSet<String> ips = new HashSet<>();
    protected HashSet<String> dates = new HashSet<>();
    protected HashSet<String> freepass = new HashSet<>();
    protected HashSet<String> strings = new HashSet<>();
    protected HashSet<String> disable = new HashSet<>();
    protected File parent;

    public String find(String entry) {
        File temp = new File(entry);
        if (temp.exists()) {
            return temp.getAbsolutePath();
        }
        temp = new File(this.parent, entry);
        if (temp.exists()) {
            return temp.getAbsolutePath();
        }
        return entry;
    }

    public Loader(String loadme, String code, Profile profile) {
        this.loadme = loadme;
        this.code = code;
        this.result = profile;
        this.parent = new File(loadme).getParentFile();
        this.parser = new ProfileParser(loadme);
        profile.addParameter(".sleeptime", "60000");
        profile.addParameter(".jitter", "0");
        profile.addParameter(".maxdns", "255");
        profile.addParameter(".useragent", "<RAND>");
        profile.addParameter(".spawnto", "rundll32.exe");
        profile.addParameter(".spawnto_x86", "%windir%\\syswow64\\rundll32.exe");
        profile.addParameter(".spawnto_x64", "%windir%\\sysnative\\rundll32.exe");
        profile.addParameter(".pipename", "msagent_##");
        profile.addParameter(".pipename_stager", "status_##");
        profile.addParameter(".sample_name", CommonUtils.strrep(new File(loadme).getName(), ".profile", ""));
        profile.addParameter(".dns_idle", "0.0.0.0");
        profile.addParameter(".dns_sleep", "0");
        profile.addParameter(".dns_stager_subhost", "");
        profile.addParameter(".dns_stager_prepend", "");
        profile.addParameter(".dns_max_txt", "252");
        profile.addParameter(".dns_ttl", "1");
        profile.addParameter(".host_stage", "true");
        profile.addParameter(".https-certificate.CN", "");
        profile.addParameter(".https-certificate.OU", "");
        profile.addParameter(".https-certificate.O", "");
        profile.addParameter(".https-certificate.L", "");
        profile.addParameter(".https-certificate.ST", "");
        profile.addParameter(".https-certificate.C", "");
        profile.addParameter(".https-certificate.validity", "3650");
        profile.addParameter(".https-certificate.keystore", "");
        profile.addParameter(".https-certificate.password", "123456");
        profile.addParameter(".http-get.verb", "GET");
        profile.addParameter(".http-post.verb", "POST");
        profile.addParameter(".code-signer.digest_algorithm", "SHA256");
        profile.addParameter(".code-signer.timestamp", "false");
        profile.addParameter(".code-signer.timestamp_mode", "AUTHENTICODE");
        profile.addParameter(".code-signer.keystore", "");
        profile.addParameter(".code-signer.password", "");
        profile.addParameter(".code-signer.alias", "");
        profile.addParameter(".code-signer.program_name", "");
        profile.addParameter(".code-signer.program_url", "");
        profile.addParameter(".code-signer.timestamp_url", "");
        profile.addParameter(".stage.checksum", "0");
        profile.addParameter(".stage.cleanup", "false");
        profile.addParameter(".stage.compile_time", "");
        profile.addParameter(".stage.entry_point", "-1");
        profile.addParameter(".stage.name", "");
        profile.addParameter(".stage.module_x86", "");
        profile.addParameter(".stage.module_x64", "");
        profile.addParameter(".stage.image_size_x86", "0");
        profile.addParameter(".stage.image_size_x64", "0");
        profile.addParameter(".stage.obfuscate", "false");
        profile.addParameter(".stage.sleep_mask", "true");
        profile.addParameter(".stage.userwx", "true");
        profile.addParameter(".stage.stomppe", "true");
        profile.addParameter(".stage.rich_header", "<DEFAULT>");
        profile.addParameter(".process-inject.min_alloc", "0");
        profile.addParameter(".process-inject.startrwx", "true");
        profile.addParameter(".process-inject.userwx", "true");
        profile.addParameter(".process-inject.CreateRemoteThread", "true");
        profile.addParameter(".process-inject.RtlCreateUserThread", "true");
        profile.addParameter(".process-inject.SetThreadContext", "true");
        profile.addParameter(".create_remote_thread", "true");
        profile.addParameter(".hijack_remote_thread", "true");
        profile.addParameter(".http-stager.uri_x86", "");
        profile.addParameter(".http-stager.uri_x64", "");
        profile.addParameter(".bind_tcp_garbage", CommonUtils.bString(CommonUtils.randomData(CommonUtils.rand(1024))));
        this.freepass.add(".http-stager.server.output");
        this.options.add(".sleeptime");
        this.options.add(".jitter");
        this.options.add(".maxdns");
        this.options.add(".http-get.uri");
        this.options.add(".http-post.uri");
        this.options.add(".http-get.verb");
        this.options.add(".http-post.verb");
        this.options.add(".useragent");
        this.options.add(".spawnto");
        this.options.add(".spawnto_x86");
        this.options.add(".spawnto_x64");
        this.options.add(".pipename");
        this.options.add(".pipename_stager");
        this.options.add(".dns_idle");
        this.options.add(".dns_sleep");
        this.options.add(".host_stage");
        this.options.add(".dns_stager_prepend");
        this.options.add(".dns_stager_subhost");
        this.options.add(".create_remote_thread");
        this.options.add(".hijack_remote_thread");
        this.options.add(".dns_max_txt");
        this.options.add(".dns_ttl");
        this.options.add(".sample_name");
        this.options.add(".stage.userwx");
        this.options.add(".stage.compile_time");
        this.options.add(".stage.checksum");
        this.options.add(".stage.cleanup");
        this.options.add(".stage.entry_point");
        this.options.add(".stage.name");
        this.options.add(".stage.obfuscate");
        this.options.add(".stage.sleep_mask");
        this.options.add(".stage.stomppe");
        this.options.add(".stage.image_size_x86");
        this.options.add(".stage.image_size_x64");
        this.options.add(".stage.module_x86");
        this.options.add(".stage.module_x64");
        this.options.add(".stage.rich_header");
        this.options.add(".process-inject.min_alloc");
        this.options.add(".process-inject.startrwx");
        this.options.add(".process-inject.userwx");
        this.strings.add(".stage.name");
        this.options.add(".https-certificate.CN");
        this.options.add(".https-certificate.OU");
        this.options.add(".https-certificate.O");
        this.options.add(".https-certificate.L");
        this.options.add(".https-certificate.ST");
        this.options.add(".https-certificate.C");
        this.options.add(".https-certificate.validity");
        this.options.add(".https-certificate.keystore");
        this.options.add(".https-certificate.password");
        this.options.add(".code-signer.keystore");
        this.options.add(".code-signer.password");
        this.options.add(".code-signer.alias");
        this.options.add(".code-signer.program_name");
        this.options.add(".code-signer.program_url");
        this.options.add(".code-signer.timestamp_url");
        this.options.add(".code-signer.timestamp_mode");
        this.options.add(".code-signer.timestamp");
        this.options.add(".code-signer.digest_algorithm");
        this.options.add(".http-stager.uri_x86");
        this.options.add(".http-stager.uri_x64");
        this.numbers.add(".sleeptime");
        this.numbers.add(".jitter");
        this.numbers.add(".maxdns");
        this.numbers.add(".dns_sleep");
        this.numbers.add(".https-certificate.validity");
        this.numbers.add(".stage.entry_point");
        this.numbers.add(".stage.image_size_x86");
        this.numbers.add(".stage.image_size_x64");
        this.numbers.add(".dns_max_txt");
        this.numbers.add(".dns_ttl");
        this.numbers.add(".process-inject.min_alloc");
        this.booleans.add(".host_http_stager");
        this.booleans.add(".code-signer.timestamp");
        this.booleans.add(".stage.userwx");
        this.booleans.add(".create_remote_thread");
        this.booleans.add(".hijack_remote_thread");
        this.booleans.add(".stage.obfuscate");
        this.booleans.add(".stage.sleep_mask");
        this.booleans.add(".stage.stomppe");
        this.booleans.add(".stage.cleanup");
        this.booleans.add(".process-inject.startrwx");
        this.booleans.add(".process-inject.userwx");
        this.files.add(".https-certificate.keystore");
        this.files.add(".code-signer.keystore");
        this.verbs.add(".http-get.verb");
        this.verbs.add(".http-post.verb");
        this.ips.add(".dns_idle");
        this.dates.add(".stage.compile_time");
        this.disable.add(".process-inject.CreateRemoteThread");
        this.disable.add(".process-inject.SetThreadContext");
        this.disable.add(".process-inject.RtlCreateUserThread");
        this.indicators.add(".http-get.server.header");
        this.indicators.add(".http-get.client.header");
        this.indicators.add(".http-get.client.parameter");
        this.indicators.add(".http-post.server.header");
        this.indicators.add(".http-post.client.header");
        this.indicators.add(".http-post.client.parameter");
        this.indicators.add(".http-stager.client.parameter");
        this.indicators.add(".http-stager.client.header");
        this.indicators.add(".http-stager.server.header");
        this.indicators.add(".stage.transform-x86.strrep");
        this.indicators.add(".stage.transform-x64.strrep");
        this.blocks.add(".http-get");
        this.blocks.add(".http-get.client");
        this.blocks.add(".http-get.client.metadata");
        this.blocks.add(".http-get.server");
        this.blocks.add(".http-get.server.output");
        this.blocks.add(".http-post");
        this.blocks.add(".http-post.client");
        this.blocks.add(".http-post.client.id");
        this.blocks.add(".http-post.client.output");
        this.blocks.add(".http-post.server");
        this.blocks.add(".http-post.server.output");
        this.blocks.add(".http-stager");
        this.blocks.add(".http-stager.client");
        this.blocks.add(".http-stager.server");
        this.blocks.add(".http-stager.server.output");
        this.blocks.add(".https-certificate");
        this.blocks.add(".code-signer");
        this.blocks.add(".stage");
        this.blocks.add(".stage.transform-x86");
        this.blocks.add(".stage.transform-x64");
        this.blocks.add(".process-inject");
        this.blocks.add(".process-inject.transform-x86");
        this.blocks.add(".process-inject.transform-x64");
        this.statementa.add(".stage.transform-x86.prepend");
        this.statementa.add(".stage.transform-x86.append");
        this.statementa.add(".stage.transform-x64.prepend");
        this.statementa.add(".stage.transform-x64.append");
        this.statementa.add(".process-inject.transform-x86.prepend");
        this.statementa.add(".process-inject.transform-x86.append");
        this.statementa.add(".process-inject.transform-x64.prepend");
        this.statementa.add(".process-inject.transform-x64.append");
        this.statementa.add(".http-stager.server.output.prepend");
        this.statementa.add(".http-stager.server.output.append");
        this.statementa.add(".stage.string");
        this.statementa.add(".stage.stringw");
        this.statementa.add(".stage.data");
        this.statementa.add(".process-inject.disable");
        this.sealme.add(".http-get.client.metadata");
        this.sealme.add(".http-get.server.output");
        this.sealme.add(".http-post.client.id");
        this.sealme.add(".http-post.client.output");
        this.sealme.add(".http-post.server.output");
        this.sealme.add(".http-stager.server.output");
        this.allowDTL(".http-get.client.metadata", this.statementb, this.statementa);
        this.allowDTL(".http-get.server.output", this.statementb, this.statementa);
        this.allowDTL(".http-post.client.id", this.statementb, this.statementa);
        this.allowDTL(".http-post.client.output", this.statementb, this.statementa);
        this.allowDTL(".http-post.server.output", this.statementb, this.statementa);
        this.statementa.add(".http-get.client.metadata.header");
        this.statementa.add(".http-get.client.metadata.parameter");
        this.statementa.add(".http-post.client.id.header");
        this.statementa.add(".http-post.client.id.parameter");
        this.statementa.add(".http-post.client.output.header");
        this.statementa.add(".http-post.client.output.parameter");
        this.statementb.add(".http-get.client.metadata.print");
        this.statementb.add(".http-get.server.output.print");
        this.statementb.add(".http-post.client.output.print");
        this.statementb.add(".http-post.client.id.print");
        this.statementb.add(".http-post.server.output.print");
        this.statementb.add(".http-stager.server.output.print");
        this.statementb.add(".http-get.client.metadata.uri-append");
        this.statementb.add(".http-post.client.id.uri-append");
        this.statementb.add(".http-post.client.output.uri-append");
    }

    private void allowDTL(String key, HashSet<String> options, HashSet<String> optionsArg) {
        options.add(key + ".base64");
        options.add(key + ".base64url");
        options.add(key + ".netbios");
        options.add(key + ".netbiosu");
        options.add(key + ".mask");
        optionsArg.add(key + ".prepend");
        optionsArg.add(key + ".append");
    }

    public void parse(String namespace) {
        String id;
        this.parse(this.code, namespace, 1);
        Iterator i = this.sealme.iterator();
        while (i.hasNext()) {
            id = i.next() + "";
            if (this.result.getProgram(id) != null || this.freepass.contains(id)) continue;
            this.parser.reportError(new SyntaxError("Profile is missing a mandatory program spec", id, 1));
        }
        i = this.options.iterator();
        while (i.hasNext()) {
            id = i.next() + "";
            if (this.result.hasString(id)) continue;
            this.parser.reportError(new SyntaxError("Profile is missing a mandatory option", id, 1));
        }
        if (this.parser.hasErrors()) {
            this.parser.resolveErrors();
        }
    }

    public void parse(String code, String namespace, int st) {
        TokenList tokens = LexicalAnalyzer.CreateTerms(this.parser, new StringIterator(code, st));
        Token[] tokena = tokens.getTokens();
        int start = 0;
        while (start < tokena.length) {
            start = this.parse(tokena, start, namespace);
        }
    }

    private static String namespace(String namespace) {
        if ("".equals(namespace)) {
            return "<Global>";
        }
        return "<" + namespace + ">";
    }

    public String convert(String text, Token token) {
        StringBuilder result = new StringBuilder();
        StringIterator si = new StringIterator(ParserUtilities.extract(text), token.getHint());
        while (si.hasNext()) {
            char current = si.next();
            if (current == '\\' && si.hasNext()) {
                int codepoint;
                String mutilate;
                current = si.next();
                if (current == 'u') {
                    if (!si.hasNext(4)) {
                        this.parser.reportErrorWithMarker("not enough remaining characters for \\uXXXX", si.getErrorToken());
                        continue;
                    }
                    mutilate = si.next(4);
                    try {
                        codepoint = Integer.parseInt(mutilate, 16);
                        result.append((char) codepoint);
                    } catch (NumberFormatException nex) {
                        this.parser.reportErrorWithMarker("invalid unicode escape \\u" + mutilate + " - must be hex digits", si.getErrorToken());
                    }
                    continue;
                }
                if (current == 'x') {
                    if (!si.hasNext(2)) {
                        this.parser.reportErrorWithMarker("not enough remaining characters for \\uXXXX", si.getErrorToken());
                        continue;
                    }
                    mutilate = si.next(2);
                    try {
                        codepoint = Integer.parseInt(mutilate, 16);
                        result.append((char) codepoint);
                    } catch (NumberFormatException nex) {
                        this.parser.reportErrorWithMarker("invalid unicode escape \\x" + mutilate + " - must be hex digits", si.getErrorToken());
                    }
                    continue;
                }
                if (current == 'n') {
                    result.append("\n");
                    continue;
                }
                if (current == 'r') {
                    result.append("\r");
                    continue;
                }
                if (current == 't') {
                    result.append("\t");
                    continue;
                }
                if (current == '\\') {
                    result.append("\\");
                    continue;
                }
                if (current == '\"') {
                    result.append("\"");
                    continue;
                }
                if (current == '\'') {
                    result.append("'");
                    continue;
                }
                this.parser.reportErrorWithMarker("unknown escape \\" + current, si.getErrorToken());
                continue;
            }
            result.append(current);
        }
        return result.toString();
    }

    public int parse(Token[] tokens, int start, String namespace) {
        String c;
        String a;
        String b;
        if (start + 3 < tokens.length) {
            String d;
            a = tokens[start].toString();
            if (Checkers.isSetStatement(a, b = tokens[start + 1].toString(), c = tokens[start + 2].toString(), d = tokens[start + 3].toString())) {
                if (!this.options.contains(namespace + "." + b)) {
                    this.parser.reportError("invalid option for " + Loader.namespace(namespace), tokens[start + 1]);
                } else {
                    String converted = this.convert(c, tokens[start + 2]);
                    if (this.numbers.contains(namespace + "." + b) && !Checkers.isNumber(converted)) {
                        this.parser.reportError("option " + Loader.namespace(namespace + "." + b) + " requires a number", tokens[start + 2]);
                    }
                    if (this.booleans.contains(namespace + "." + b) && !Checkers.isBoolean(converted)) {
                        this.parser.reportError("option " + Loader.namespace(namespace + "." + b) + " requires true or false", tokens[start + 2]);
                    }
                    if (this.verbs.contains(namespace + "." + b) && !Checkers.isHTTPVerb(converted)) {
                        this.parser.reportError("option " + Loader.namespace(namespace + "." + b) + " requires a valid HTTP verb", tokens[start + 2]);
                    }
                    if (this.ips.contains(namespace + "." + b) && !CommonUtils.isIP(converted)) {
                        this.parser.reportError("option " + Loader.namespace(namespace + "." + b) + " requires an IPv4 address", tokens[start + 2]);
                    }
                    if (this.dates.contains(namespace + "." + b) && !Checkers.isDate(converted)) {
                        this.parser.reportError("option " + Loader.namespace(namespace + "." + b) + " requires a 'dd MMM YYYY hh:mm:ss' date", tokens[start + 2]);
                    }
                    if (this.files.contains(namespace + "." + b)) {
                        String location = this.find(converted);
                        if (new File(location).exists()) {
                            this.result.addParameter(namespace + "." + b, location);
                        } else {
                            this.parser.reportError("could not find file in " + Loader.namespace(namespace + "." + b), tokens[start + 2]);
                        }
                    } else if (this.strings.contains(namespace + "." + b)) {
                        this.result.addToString(namespace, CommonUtils.toBytes(converted + '\u0000'));
                        this.result.addToString(namespace, CommonUtils.randomDataNoZeros(5));
                        this.result.addParameter(namespace + "." + b, converted);
                    } else {
                        this.result.addParameter(namespace + "." + b, converted);
                    }
                }
                return start + 4;
            }
            if (Checkers.isIndicator(a, b, c, d)) {
                String key = this.convert(b, tokens[start + 1]);
                StringBuilder value = new StringBuilder(this.convert(c, tokens[start + 2]));
                if (!this.indicators.contains(namespace + "." + a)) {
                    if ("strrep".equals(a)) {
                        this.parser.reportError("invalid token for " + Loader.namespace(namespace), tokens[start]);
                    } else {
                        this.parser.reportError("invalid indicator for " + Loader.namespace(namespace), tokens[start]);
                    }
                } else{
                    switch (a) {
                        case "header":
                            this.result.addCommand(namespace, "!" + a, key + ": " + value);
                            break;
                        case "parameter":
                            this.result.addCommand(namespace, "!" + a, key + "=" + value);
                            break;
                        case "strrep":
                            if (value.length() > key.length()) {
                                this.parser.reportError("strrep length(original) < length(replacement value). I can't do this.", tokens[start + 2]);
                            } else {
                                while (value.length() < key.length()) {
                                    value.append('\u0000');
                                }
                                this.result.addCommand(namespace, a, key + value);
                            }
                            break;
                    }
                }
                return start + 4;
            }
        }
        if (start + 2 < tokens.length && Checkers.isStatementArg(a = tokens[start].toString(), b = tokens[start + 1].toString(), c = tokens[start + 2].toString())) {
            String text;
            if (!this.statementa.contains(namespace + "." + a)) {
                this.parser.reportError("Statement with argument is not valid for " + Loader.namespace(namespace), tokens[start]);
            } else if (this.result.isSealed(namespace)) {
                this.parser.reportError("Program is terminated. Can't add transform statements to " + Loader.namespace(namespace), tokens[start]);
            } else {
                switch (a) {
                    case "string":
                        text = this.convert(b, tokens[start + 1]) + '\u0000';
                        this.result.addToString(namespace, CommonUtils.toBytes(text));
                        this.result.logToString(namespace, text);
                        break;
                    case "stringw":
                        text = this.convert(b, tokens[start + 1]) + '\u0000';
                        this.result.addToString(namespace, CommonUtils.toBytes(text, "UTF-16LE"));
                        this.result.logToString(namespace, text);
                        break;
                    case "data":
                        text = this.convert(b, tokens[start + 1]);
                        this.result.addToString(namespace, CommonUtils.toBytes(text));
                        break;
                    case "disable":
                        String converted = this.convert(b, tokens[start + 1]);
                        if (!this.disable.contains(namespace + "." + converted)) {
                            this.parser.reportError("function " + converted + " is not a recognized disable option", tokens[start + 1]);
                        } else {
                            this.result.addParameter(namespace + "." + converted, "false");
                        }
                        break;
                    default:
                        this.result.addCommand(namespace, a, this.convert(b, tokens[start + 1]));
                }
            }
            return start + 3;
        }
        if (start + 1 < tokens.length) {
            a = tokens[start].toString();
            if (Checkers.isStatementBlock(a, b = tokens[start + 1].toString())) {
                if (!this.blocks.contains(namespace + "." + a)) {
                    this.parser.reportError("Block is not valid for " + Loader.namespace(namespace), tokens[start]);
                } else {
                    this.parse(ParserUtilities.extract(b), namespace + "." + a, tokens[start + 1].getHint());
                    this.result.addCommand(namespace, "build", namespace + "." + a);
                    if (this.sealme.contains(namespace + "." + a) && !this.result.isSealed(namespace + "." + a)) {
                        this.parser.reportError("Program " + Loader.namespace(namespace + "." + a) + " must end with a termination statement", tokens[start + 1]);
                    }
                }
                return start + 2;
            }
            if (Checkers.isStatement(a, b)) {
                if (!this.statementb.contains(namespace + "." + a)) {
                    this.parser.reportError("Statement is not valid for " + Loader.namespace(namespace), tokens[start]);
                } else if (this.result.isSealed(namespace)) {
                    this.parser.reportError("Program is terminated. Can't add transform statements to " + Loader.namespace(namespace), tokens[start]);
                } else {
                    this.result.addCommand(namespace, a, null);
                }
                return start + 2;
            }
        }
        if (start < tokens.length) {
            a = tokens[start].toString();
            if (Checkers.isComment(a)) {
                return start + 1;
            }
            this.parser.reportError("Unknown statement in " + Loader.namespace(namespace), tokens[start]);
            return 10000;
        }
        return 0;
    }

    public static Profile LoadDefaultProfile() {
        InputStream i = Loader.class.getClassLoader().getResourceAsStream("resources/default.profile");
        return Loader.LoadProfile("default", i);
    }

    public static Profile LoadProfile(String loadme) {
        try {
            File afile = new File(loadme);
            return Loader.LoadProfile(loadme, new FileInputStream(afile));
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Profile LoadProfile(String loadme, InputStream i) {
        try {
            BufferedReader temp = new BufferedReader(new InputStreamReader(i));
            String data = temp.lines().map(text -> text + '\n').collect(Collectors.joining());
            temp.close();
            Profile profile = new Profile();
            Loader loader = new Loader(loadme, data, profile);
            loader.parse("");
            if (loader.parser.hasErrors()) {
                return null;
            }
            return profile;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static class ProfileParser
            extends Parser {
        public ProfileParser(String name) {
            super(name, "");
        }

        public void resolveErrors() throws YourCodeSucksException {
            if (this.hasErrors()) {
                CommonUtils.print_error("Error(s) while compiling " + this.name);
                this.errors.addAll(this.warnings);
                YourCodeSucksException yex = new YourCodeSucksException(this.errors);
                yex.printErrors(System.out);
            }
        }
    }

}


import 'dart:async';

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter/services.dart';
import 'package:silence_please/config/config.dart';

import '../util/plugin.dart';

class SettingsPage extends StatefulWidget {
  @override
  _SettingsPageState createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  static const DEFAULT_MESSAGE = 0;
  static const DEFAULT_ATTEMPTS = 1;
  bool isServiceDisable = true;

  bool enableSmsService = false;
  bool enableWhiteListService = false;

  String defaultSms = "";
  String defaultAttempts = "";

  EventChannel eventChannel = PluginHandShake.serviceEventChannel;

  @override
  void initState() {
    super.initState();
    eventChannel.receiveBroadcastStream().listen(_onServiceStatusChange) ;
    getHelpers();
  }

  @override
  Widget build(BuildContext context) {
    return new Scaffold(
      backgroundColor: Colors.white,
      appBar: new AppBar(
        title: new Text("Settings"),
        leading: new Icon(Icons.settings),
      ),
      body: new Builder(
        builder: (context) {
          return new Theme(
            data: Theme.of(context).copyWith(brightness: Brightness.light),
            child: new ListView(
              children: <Widget>[
                new Padding(
                  padding: const EdgeInsets.symmetric(vertical: 6.0),
                  child: new ExpansionTile(
                    title: new ListTile(
                      title: new Text(
                        "Enable SMS Service",
                        style: Theme
                            .of(context)
                            .textTheme
                            .button
                            .copyWith(color: Colors.black, fontSize: 18.0),
                      ),
                      subtitle: new Text(
                          "Send SMS to caller to notify them you are not available " +
                              "\n\nNote this service is required a phone state and send sms permissions"),
                    ),
                    children: <Widget>[
                      isServiceDisable?
                        new CheckboxListTile(
                          value: enableSmsService,
                          onChanged: isServiceDisable? (value) {
                            setState(() {
                              toggleSmsService(value);
                            });
                          }: null,
                          title: new Text("Enable service"),
                          subtitle: new Text(
                            "Enable sms service"
                          ),
                        )
                      :
                        new ListTile(
                          title: new Text("Enable service"),
                          subtitle: new Text(
                          "You cannot change it when silence is active"
                          )
                        )
                      ,
                      new ListTile(
                        title: new Text("Change default sms"),
                        subtitle: new Text(defaultSms),
                        onTap: () => showInputSettingDialog(
                            context: context, which: DEFAULT_MESSAGE),
                      ),
                      new ListTile(
                        title: new Text("Change default call attempts"),
                        subtitle: new Text(defaultAttempts),
                        onTap: () => showInputSettingDialog(
                            context: context, which: DEFAULT_ATTEMPTS),
                      ),
                    ],
                  ),
                ),
                new Padding(
                  padding: const EdgeInsets.symmetric(vertical: 6.0),
                  child: new ExpansionTile(
                    title: new ListTile(
                      title: new Text(
                        "Enable White List Service",
                        style: Theme
                            .of(context)
                            .textTheme
                            .button
                            .copyWith(color: Colors.black, fontSize: 18.0),
                      ),
                      subtitle: new Text(
                          "Ring my phone when white listed phone number contact me" +
                          "\n\nNote this service is required a read contact, phone state " +
                          "and read external storage(to get your default ringtone) permissions"),
                    ),
                    children: <Widget>[
                      isServiceDisable?
                        new CheckboxListTile(
                          value: enableWhiteListService,
                          onChanged: isServiceDisable? (value) {
                            setState(() {
                              toggleWhiteListService(value);
                            });
                          }: null,
                          title: new Text("Enable service"),
                          subtitle: new Text(
                            "Enable white service service"
                          ),
                        )
                      :
                        new ListTile(
                          title: new Text("Enable service"),
                          subtitle: new Text(
                          "You cannot change it when silence is active"
                          )
                        ),
                      new ListTile(
                        title: new Text("White listed contact"),
                        onTap: () => Navigator.pushNamed(context, "/whitelist"),
                      ),
                    ],
                  ),
                ),
                new Padding(
                  padding: const EdgeInsets.symmetric(vertical: 6.0),
                  child: new ListTile(
                    title: new Text("About"),
                    onTap: () => Navigator.pushNamed(context, "/about"),
                  ),
                ),
                new Padding(
                  padding: const EdgeInsets.symmetric(vertical: 6.0),
                  child: new ListTile(
                    title: new Text("Licenses"),
                    onTap: () => Navigator.pushNamed(context, "/license"),
                  ),
                )
              ],
            ),
          );
        },
      ),
    );
  }

  void getHelpers() async {
    SharedPreferences pref = await SharedPreferences.getInstance();
    setState(() {
      defaultSms = pref.getString(AppConfig.SMS_SERVICE_MESSAGE);
      defaultAttempts =
          pref.getInt(AppConfig.SMS_SERVICE_ATTEMPTS).toString();
      enableSmsService = pref.getBool(AppConfig.SMS_SERVICE_ENABLE);
      enableWhiteListService = pref.getBool(AppConfig.WHITE_LIST_SERVICE);
    });
  }

  void _onServiceStatusChange(Object event) async {
    if (mounted) {
      setState(() {
        isServiceDisable = event == "service-stopped";   
      });
    }
  }

  void toggleSmsService(bool curValue) async {
    SharedPreferences pref = await SharedPreferences.getInstance();
    String result =
        await PluginHandShake().checkPermission(PluginHandShake.SMS_SERVICE);
    if (result == "granted") {
      await pref.setBool(AppConfig.SMS_SERVICE_ENABLE, curValue);
      setState(() {
        enableSmsService = curValue;
      });
    } else if (result.contains("rationale.")) {
      var resultSplit = result.split(".");
      if (resultSplit.length == 1) {
      } else {
        var which = resultSplit[1];
        if (which == "12") {
          showHelperDialog(
              context: context,
              text:
                  "You havn't give me a Send SMS and Read Phone State permission.\n" +
                      "Go to Setting provide those permissions");
        } else {
          if (which == "1") {
            showHelperDialog(
                context: context,
                text: "You havn't give me a Read Phone State permission.\n" +
                    "Go to Setting provide this permission");
          } else if (which == "2") {
            showHelperDialog(
                context: context,
                text: "You havn't give me a Send SMS permission.\n" +
                    "Go to Setting provide this permission");
          }
        }
      }
    }
  }

  void toggleWhiteListService(bool curValue) async {
    SharedPreferences pref = await SharedPreferences.getInstance();
    String result =
        await PluginHandShake().checkPermission(PluginHandShake.READ_CONTACT);
    if (result == "granted") {
      await pref.setBool(AppConfig.WHITE_LIST_SERVICE, curValue);
      setState(() {
        enableWhiteListService = curValue;
      });
    } else if (result.contains("rationale.")) {
      var resultSplit = result.split(".");
      if (resultSplit.length == 1) {
      } else {
        var which = resultSplit[1];
        if (which == "123") {
          showHelperDialog(
              context: context,
              text:
                  "You havn't give me a Read Contact and Read Phone State permission.\n" +
                      "Go to Setting provide those permissions");
        } else {
          if (which == "1") {
            showHelperDialog(
                context: context,
                text: "You havn't give me a Read Phone State permission.\n" +
                    "Go to Setting provide this permission");
          } else if (which == "2") {
            showHelperDialog(
                context: context,
                text: "You havn't give me a Read Contact permission.\n" +
                    "Go to Setting provide this permission");
          } else if (which == "3") {
            showHelperDialog(
                context: context,
                text: "You havn't give me a Read External Storage permission.\n" +
                    "Go to Setting provide this permission");
          }
        }
      }
    }
  }
 
  void redirect() async {
    PluginHandShake().redirectPermisionsSettings();
  }

  Future<Null> showHelperDialog({BuildContext context, String text}) async {
    return showDialog(
        context: context,
        builder: (context) {
          return new SimpleDialog(
            title: new Text(
              "Permission required",
              style: Theme.of(context).textTheme.body1.copyWith(fontSize: 25.0),
            ),
            children: <Widget>[
              new Padding(
                padding: const EdgeInsets.all(8.0),
                child: new Text(text,
                    style: Theme
                        .of(context)
                        .textTheme
                        .button
                        .copyWith(fontSize: 18.0)),
              ),
              new Align(
                alignment: Alignment.centerRight,
                child: new FlatButton(
                    child: new Text(
                      "Update",
                      style: Theme
                          .of(context)
                          .textTheme
                          .body1
                          .copyWith(fontSize: 18.0),
                    ),
                    onPressed: () {
                      redirect();
                    }),
              )
            ],
          );
        });
  }

  void updatePref(String value, which) async {
    SharedPreferences pref = await SharedPreferences.getInstance();
    if (which == _SettingsPageState.DEFAULT_MESSAGE) {
      await pref.setString(AppConfig.SMS_SERVICE_MESSAGE, value);
      setState(() {
        getHelpers();
      });
    } else if (which == _SettingsPageState.DEFAULT_ATTEMPTS) {
      await pref.setInt(AppConfig.SMS_SERVICE_ATTEMPTS, int.parse(value));
      setState(() {
        getHelpers();
      });
    }
  }

  Future<Null> showInputSettingDialog({BuildContext context, int which}) async {
    final textController = new TextEditingController();
    final formKey = new GlobalKey<FormState>();
    return await showDialog(
        context: context,
        builder: (context) {
          return new Container(
            width: double.infinity,
            child: new SimpleDialog(
              title: new Text(
                "Update",
                style:
                    Theme.of(context).textTheme.body1.copyWith(fontSize: 25.0),
              ),
              children: <Widget>[
                new Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: new Form(
                    key: formKey,
                    autovalidate: true,
                    child: new TextFormField(
                      keyboardType: which == _SettingsPageState.DEFAULT_ATTEMPTS
                          ? TextInputType.number
                          : TextInputType.text,
                      decoration: const InputDecoration(
                        hintText: "Enter new value",
                        helperText: "Enter new value",
                        border: const OutlineInputBorder(),
                      ),
                      style: Theme
                          .of(context)
                          .textTheme
                          .body1
                          .copyWith(fontSize: 18.0),
                      autofocus: true,
                      controller: textController,
                      maxLines:
                          which == _SettingsPageState.DEFAULT_ATTEMPTS ? 1 : 4,
                      validator: (value) {
                        if (value.isEmpty) {
                          return "Value cannot be empty";
                        }
                        if (which == _SettingsPageState.DEFAULT_ATTEMPTS) {
                          if (value.startsWith("0")) {
                            return "Attempts must be greator than 0";
                          }
                          try {
                            int.parse(value);
                          } catch (e) {
                            return "Attempts must be whole number";
                          }
                        }
                      },
                    ),
                  ),
                ),
                new Align(
                  alignment: Alignment.centerRight,
                  child: new FlatButton(
                      child: new Text(
                        "Update",
                        style: Theme
                            .of(context)
                            .textTheme
                            .body1
                            .copyWith(fontSize: 18.0),
                      ),
                      onPressed: () {
                        if (formKey.currentState.validate()) {
                          updatePref(textController.value.text, which);
                          Navigator.pop(context);
                        }
                      }),
                )
              ],
            ),
          );
        });
  }
}

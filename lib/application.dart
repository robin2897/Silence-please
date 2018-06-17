import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:redux/redux.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:silence_please/config/config.dart';
import 'package:silence_please/pages/about.dart';
import 'package:silence_please/pages/home.dart';
import 'package:silence_please/pages/license.dart';
import 'package:silence_please/pages/settings.dart';
import 'package:silence_please/pages/splash.dart';
import 'package:silence_please/pages/white-list.dart';
import 'package:silence_please/redux/state-manager.dart';
import 'package:silence_please/util/fonts-colors.dart';

import 'redux/middleware.dart';

class Application extends StatelessWidget {
  final Store<AppState> store = Store<AppState>(reducer,
      initialState: AppState.init(), middleware: [actionsMiddleware]);

  void firstRun() async {
    SharedPreferences pref = await SharedPreferences.getInstance();
    try {
      var isfirst = pref.getBool("IS_FIRST");
      if (isfirst == null) {
        await pref.setBool(AppConfig.SILENCE_IS_ENABLE, true);
        await pref.setBool(AppConfig.SMS_SERVICE_ENABLE, false);
        if (AppConfig.flavor == Flavor.PAID) {
          await pref.setString(
            AppConfig.SMS_SERVICE_MESSAGE, "I'm busy call me later");
        } else {
          await pref.setString(
            AppConfig.SMS_SERVICE_MESSAGE, "I'm busy call me later"+ AppConfig.WATERMARK);
        }        
        await pref.setInt(AppConfig.SMS_SERVICE_ATTEMPTS, 3);
        await pref.setBool(AppConfig.IS_SILENCE_ACTIVE, false);
        await pref.setBool(AppConfig.SMS_SERVICE_ENABLE_TEMP, false);
        await pref.setBool(AppConfig.WHITE_LIST_SERVICE, false);
        await pref.setBool("IS_FIRST", false);
      }
    } catch (e) {}
  }

  @override
  Widget build(BuildContext context) {
    firstRun();
    final ThemeData _theme = new ThemeData(
        brightness: Brightness.dark,
        primaryColor: BaseColors.primary,
        primaryColorLight: BaseColors.primaryLight,
        primaryColorDark: BaseColors.primaryDark,
        accentColor: BaseColors.secondary,
        indicatorColor: BaseColors.secondaryDark,
        dividerColor: Color(0xFF707070).withOpacity(0.5),
        scaffoldBackgroundColor: Colors.transparent,
        textTheme: Theme.of(context).textTheme.copyWith(
            body1: new TextStyle(
                fontFamily: BaseFonts.philosopher, fontWeight: FontWeight.w400),
            title: new TextStyle(fontFamily: BaseFonts.playball),
            button: new TextStyle(
                fontFamily: BaseFonts.philosopher,
                fontWeight: FontWeight.bold)));

    return new StoreProvider(
      store: store,
      child: new StoreConnector<AppState, void>(
        onInit: (store) => store.dispatch(InitiateAction()),
        converter: (store) => {},
        builder: (context, _) => new MaterialApp(
              title: 'Silence please',
              theme: _theme,
              home: new SplashPage(),
              routes: <String, WidgetBuilder>{
                "/home": (BuildContext context) => HomePage(),
                "/license": (BuildContext context) => LicenseScreen(),
                "/about": (BuildContext context) => AboutPage(),
                "/whitelist": (BuildContext context) => WhiteList(),
                "/settings": (BuildContext context) => SettingsPage()
              },
            ),
      ),
    );
  }
}

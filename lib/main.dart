import 'package:flutter/material.dart';
import 'package:redux/redux.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'pages/about.dart';
import 'pages/home.dart';
import 'pages/license.dart';
import 'pages/splash.dart';
import 'redux/state-manager.dart';
import 'package:flutter_redux/flutter_redux.dart';

import 'util/fonts-colors.dart';
import 'redux/middleware.dart';

void main() => runApp(new Application());

class Application extends StatelessWidget {
  final Store<AppState> store = Store<AppState>(reducer,
      initialState: AppState.init(), middleware: [actionsMiddleware]);

  void isFirstRun() async {
    SharedPreferences pref = await SharedPreferences.getInstance();
    try {
      var isfirst = pref.getBool("IS_FIRST");
      if(isfirst == null) {        
        await pref.setBool("IS_ENABLE", true);
        await pref.setBool("IS_FIRST", false);
      }
    } catch (e) {
    } 
  }

  @override
  Widget build(BuildContext context) {
    isFirstRun();
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
        builder: (cxt, _) => new MaterialApp(
              title: 'Silence please',
              theme: _theme,
              home: new SplashPage(),
              routes: <String, WidgetBuilder>{
                "/home": (BuildContext context) => HomePage(),
                "/license": (BuildContext context) => LicenseScreen(),
                "/about": (BuildContext context) => AboutPage()
              },
            ),
      ),
    );
  }
}

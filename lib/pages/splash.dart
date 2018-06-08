import 'package:flutter/material.dart';

import 'dart:async';

class SplashPage extends StatefulWidget {
  @override
  _SplashPageState createState() => new _SplashPageState();
}

class _SplashPageState extends State<SplashPage> {

  @override
  void initState() {
    super.initState();
    startTimer();
  }

  startTimer() async {
    new Timer(const Duration(milliseconds: 1500), () =>
     Navigator.pushReplacementNamed(context, "/home"));
  }

  @override
  Widget build(BuildContext context) {
    return new Container(
      decoration: new BoxDecoration(
        gradient: new LinearGradient(
          colors: [
            Color(0xFF304FFE),
            Color(0xFF18287F)
          ],
          begin: FractionalOffset.topLeft,
          end: FractionalOffset.bottomRight 
        )
      ),
      child: new Stack(
        fit: StackFit.expand,
        children: <Widget>[
           new Center(
            child: new FractionallySizedBox(
              heightFactor: 0.5,
              widthFactor: 0.5,
              child: new Image.asset("assets/image/splash_logo.png",),
            ),
          ),
          new FractionalTranslation(
            translation: new Offset(0.0, 0.35),
            child: new Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: <Widget>[
                new Text("Loading",
                  style: Theme.of(context).textTheme.body1.copyWith(fontSize: 16.0), 
                ),
                new Padding(
                  padding: const EdgeInsets.only(top: 8.0),
                  child: new CircularProgressIndicator(
                    valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                  ),
                )
              ],
            ),
          )
        ],
      )
    );
  }
}

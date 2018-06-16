import 'package:flutter/material.dart';
import 'package:silence_please/config/config.dart';
import 'package:silence_please/main.dart';

class AboutPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return new Container(
        decoration: new BoxDecoration(
            gradient: new LinearGradient(
                colors: [Color(0xFF304FFE), Color(0xFF18287F)],
                begin: FractionalOffset.topLeft,
                end: FractionalOffset.bottomRight)),
        child: new Stack(
          fit: StackFit.expand,
          children: <Widget>[
            new Center(
              child: new FractionallySizedBox(
                heightFactor: 0.5,
                widthFactor: 0.5,
                child: new Image.asset(
                  "assets/image/splash_logo.png",
                ),
              ),
            ),
            new FractionalTranslation(
              translation: new Offset(0.0, 0.35),
              child: new Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: <Widget>[
                  new Text(
                    AppConfig.VERSION,
                    style: Theme
                        .of(context)
                        .textTheme
                        .body1
                        .copyWith(fontSize: 16.0),
                  ),
                  new Padding(
                    padding: const EdgeInsets.symmetric(vertical: 6.0),
                    child: new Text(
                      "Created by: RIMS",
                      style: Theme
                          .of(context)
                          .textTheme
                          .body1
                          .copyWith(fontSize: 16.0),
                    ),
                  )
                ],
              ),
            )
          ],
        ));
  }
}

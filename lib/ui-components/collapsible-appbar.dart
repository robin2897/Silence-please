import 'dart:async';

import 'package:flutter/material.dart';

import '../model/appmodel.dart';
import '../util/plugin.dart';
import '../util/sperator.dart';

class CollapsibleAppbar extends StatefulWidget {

  @override
  CollapsibleAppbarState createState() {
    return new CollapsibleAppbarState();
  }
}

class CollapsibleAppbarState extends State<CollapsibleAppbar> {
  @override
  Widget build(BuildContext context) {
    return new SliverAppBar(
      backgroundColor: Colors.transparent,
      expandedHeight: 210.0,
      flexibleSpace: new FlexibleSpaceBar(
          background: new Center(
        child: new Column(
          mainAxisAlignment: MainAxisAlignment.start,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: <Widget>[
            new NextSilence(),
            new Text(
              "Next silence",
              style: Theme.of(context).textTheme.body1.copyWith(fontSize: 18.0),
            )
          ],
        ),
      )),
    );
  }
}

class NextSilence extends StatefulWidget {

  @override
  NextSilenceState createState() {
    return new NextSilenceState();
  }
}

class NextSilenceState extends State<NextSilence> {

  Future<dynamic> currentValue() async { 
    var result = await PluginHandShake().nextDB;
    return result;
  }

  @override
  Widget build(BuildContext context) {
    return new FutureBuilder(
      future: currentValue(),
      builder: (cxt, snapshot) {
          switch(snapshot.connectionState) {
            case ConnectionState.done:
              var data = snapshot.data;
              if(data is AppModel) {
                TimeExtractor extractor = timeExtractor(rawTime:
                  new TimeOfDay.fromDateTime(data.startTime), context: context);
                return new Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.baseline,
                  textBaseline: TextBaseline.alphabetic,
                  children: <Widget>[
                    new Text(
                      extractor.time,
                      style: Theme
                          .of(context)
                          .textTheme
                          .body1
                          .copyWith(fontSize: 80.0),
                    ),
                    new Text(
                      extractor.suffix,
                      style: Theme
                          .of(context)
                          .textTheme
                          .body1
                          .copyWith(fontSize: 25.0),
                    )
                  ],
                );
              } else {
                return new Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.baseline,
                  textBaseline: TextBaseline.alphabetic,
                  children: <Widget>[
                    new Text(
                      "No Silence Added",
                      style: Theme
                          .of(context)
                          .textTheme
                          .body1
                          .copyWith(fontSize: 40.0),
                    )
                  ],
                );
              }
              break;
            default: return new Text(
              "00:00",
              style: Theme
                .of(context)
                .textTheme
                .body1
                .copyWith(fontSize: 80.0, color: Colors.transparent),
            );
          }
      },
    );
  }
}

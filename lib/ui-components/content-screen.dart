import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:meta/meta.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:silence_please/config/config.dart';

import '../model/appmodel.dart';
import '../redux/state-manager.dart';
import '../util/menu-controller.dart';
import '../util/plugin.dart';
import 'collapsible-appbar.dart';
import 'curved-background.dart';
import 'drop-down-scaffold.dart';
import 'floating-action.dart';
import 'list.dart';

class FrontContent extends StatefulWidget {
  @override
  _FrontContentState createState() => new _FrontContentState();
}

class _FrontContentState extends State<FrontContent> with WidgetsBindingObserver {
  Map<String, GlobalKey> key;
  bool isNotificationPolicy = true;

  void checkPermission() async {
    String result = await PluginHandShake()
        .checkPermission(PluginHandShake.NOTIFICATION_POLICY);
    setState(() {
      isNotificationPolicy = result == "granted@";
    });
  }

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    checkPermission();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      setState(() {checkPermission();});
    }
  }

  @override
  Widget build(BuildContext context) {
    return new DropDownScaffoldProvider(builder: (cxt, controller, keys) {
      key = keys;
      return homeScreen(controller, keys);
    });
  }

  PreferredSizeWidget appbar(
      {@required BuildContext context, @required MenuController controller}) {
    return new AppBar(
      title: new Text("Silence",
          style: Theme.of(context).textTheme.title.copyWith(fontSize: 35.0)),
      centerTitle: true,
      backgroundColor: Colors.transparent,
      elevation: 0.0,
      leading: new IconButton(
          icon: Icon(Icons.menu),
          onPressed: () {
            controller.toggle();
            setState(() {});
          }),
    );
  }

  Widget homeScreen(MenuController controller,
      Map<String, GlobalKey<State<StatefulWidget>>> key) {
    return new Container(
        color: Color(0xFFEEEEEE),
        child: new Stack(
          fit: StackFit.expand,
          children: <Widget>[
            new CurvedBackground(),
            new Scaffold(
              key: key["scaffold_key"],
              appBar: appbar(context: context, controller: controller),
              floatingActionButton: isNotificationPolicy
                  ? new FloatingAction(scaffoldKey: key["scaffold_key"])
                  : null,
              floatingActionButtonLocation: isNotificationPolicy
                  ? FloatingActionButtonLocation.centerFloat
                  : null,
              body: isNotificationPolicy
                  ? new FutureBuilder(
                      future: getIsEnable(),
                      builder: (cxt, snap) {
                        switch (snap.connectionState) {
                          case ConnectionState.done:
                            var value = snap.data;
                            if (value) {
                              return new StoreConnector<AppState,
                                  List<AppModel>>(
                                converter: (store) => store.state.items,
                                builder: (cxt, viewModel) {
                                  Widget child = viewModel.length == 0
                                      ? new Center(
                                          child: new FractionallySizedBox(
                                              heightFactor: 0.5,
                                              widthFactor: 0.5,
                                              child: new Image.asset(
                                                  "assets/image/robo.png")),
                                        )
                                      : new NestedScrollView(
                                          headerSliverBuilder:
                                              (cxt, innerBoxIsScrolled) {
                                            return <Widget>[
                                              new CollapsibleAppbar()
                                            ];
                                          },
                                          body: TimeList(),
                                        );
                                  return child;
                                },
                              );
                            } else {
                              return new Container(
                                  alignment: Alignment.topCenter,
                                  margin: const EdgeInsets.only(top: 18.0),
                                  child: new Text(
                                    "Silence is disabled \n Enable it from dropdown menu",
                                    textAlign: TextAlign.center,
                                    style: Theme
                                        .of(context)
                                        .textTheme
                                        .body1
                                        .copyWith(fontSize: 25.0),
                                  ));
                            }
                            break;
                          default:
                            return new Container();
                        }
                      },
                    )
                  : new Container(
                      alignment: Alignment.topCenter,
                      margin: const EdgeInsets.only(top: 10.0),
                      child: new Text(
                        "Silence is disabled \n Required NOTIFICATION POLICY ACCESS permission",
                        textAlign: TextAlign.center,
                        style: Theme
                            .of(context)
                            .textTheme
                            .body1
                            .copyWith(fontSize: 25.0),
                      )),
            )
          ],
        ));
  }

  Future<dynamic> getIsEnable() async {
    SharedPreferences pref = await SharedPreferences.getInstance();
    return pref.getBool(AppConfig.SILENCE_IS_ENABLE);
  }
}

typedef Widget KeyProviderBuilder(
    BuildContext context, Map<String, GlobalKey> keys);

class KeyProvider extends StatelessWidget {
  final KeyProviderBuilder builder;
  KeyProvider({@required this.builder});

  @override
  Widget build(BuildContext context) {
    return builder(context, getKeys(context));
  }

  Map<String, GlobalKey> getKeys(BuildContext context) {
    var state =
        context.ancestorStateOfType(new TypeMatcher<_FrontContentState>())
            as _FrontContentState;
    return state.key;
  }
}

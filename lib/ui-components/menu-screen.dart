import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:silence_please/config/config.dart';

import '../util/fonts-colors.dart';
import '../util/menu-controller.dart';
import '../util/plugin.dart';
import 'drop-down-scaffold.dart';

class MenuScreen extends StatefulWidget {

  @override
  _MenuScreenState createState() => new _MenuScreenState();
}

class _MenuScreenState extends State<MenuScreen>
    with SingleTickerProviderStateMixin {
  bool enableStatus = true;
  Animation<Offset> _animation;
  AnimationController _aController;

  @override
  void initState() {
    super.initState();
    _aController = new AnimationController(
        vsync: this, duration: const Duration(milliseconds: 200));
    _animation =
        new Tween<Offset>(begin: Offset.zero, end: const Offset(0.7, 0.0))
            .animate(_aController)
              ..addListener(() => setState(() => {}))
              ..addStatusListener((status) {
                switch (status) {
                  case AnimationStatus.completed:
                    enableStatus = true;
                    break;
                  case AnimationStatus.dismissed:
                    enableStatus = false;
                    break;
                  default:
                    break;
                }
              });
    getEnableStatus();
  }

  @override
  void dispose() {
    _aController.dispose();
    super.dispose();
  }

  void getEnableStatus() async {
    SharedPreferences pref = await SharedPreferences.getInstance();
    var result = pref.getBool(AppConfig.SILENCE_IS_ENABLE);
    setState(() {
      enableStatus = result;
      enableStatus? _aController.forward(): _aController.reverse();
    });
  }

  @override
  Widget build(BuildContext context) {
    enableStatus ? _aController.forward() : null;
    return new DropDownScaffoldProvider(
      builder: (cxt, menu, keys) {
        return new Container(
          decoration: new BoxDecoration(color: BaseColors.primaryDark),
          child: new Material(
            color: Colors.transparent,
            child: new Padding(
              padding: const EdgeInsets.only(top: 50.0),
              child: new Column(
                mainAxisAlignment: MainAxisAlignment.start,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: <Widget>[
                  customEnableDisableSwitch(keys),
                  mainMenu(menu),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  Widget mainMenu(MenuController controller) {
    return new Column(
      mainAxisAlignment: MainAxisAlignment.start,
      crossAxisAlignment: CrossAxisAlignment.center,
      children: <Widget>[
        menuItem(
            label: "Settings",
            onTap: () {
              controller.close();              
              Navigator.pushNamed(context, "/settings");
            }),
        menuItem(
            label: "About",
            onTap: () {
              controller.close();
              Navigator.pushNamed(context, "/about");
            }),
        menuItem(
            label: "Licenses",
            onTap: () {
              controller.close();
              Navigator.pushNamed(context, "/license");
            })
      ],
    );
  }

  Widget menuItem({String label, Function onTap}) {
    return new Padding(
      padding: const EdgeInsets.only(top: 8.0),
      child: new InkWell(
        onTap: onTap,
        child: new Container(
          width: double.infinity,
          height: 45.0,
          child: new Row(
            mainAxisAlignment: MainAxisAlignment.start,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              new Container(
                height: double.infinity,
                width: 5.0,
                color: Colors.white,
              ),
              new Expanded(
                child: new Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: <Widget>[
                    new Center(
                        child: new Text(
                      label,
                      style: Theme
                          .of(context)
                          .textTheme
                          .button
                          .copyWith(fontSize: 18.0, color: Colors.white),
                    ))
                  ],
                ),
              )
            ],
          ),
        ),
      ),
    );
  }

  Widget customEnableDisableSwitch(Map<String, GlobalKey> keys) {
    return new Container(
      width: 220.0,
      height: 65.0,
      decoration: new BoxDecoration(
          borderRadius: new BorderRadius.circular(50.0),
          color: BaseColors.primaryLight.withOpacity(0.5)),
      child: new GestureDetector(
        onTap: () {
          setState(() {
            enableStatus ? _aController.reverse() : _aController.forward();
            enableStatus = enableStatus ? false : true;
            saveEnableDisableSetting(value: enableStatus);
          });
        },
        child: new Stack(
          fit: StackFit.loose,
          children: <Widget>[
            new Center(
              child: Text(enableStatus ? "Enabled" : "Disabled",
                  style: Theme
                      .of(context)
                      .textTheme
                      .button
                      .copyWith(fontSize: 18.0)),
            ),
            new SlideTransition(
              position: _animation,
              child: new Row(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: <Widget>[
                  new Padding(
                      padding: const EdgeInsets.all(4.0),
                      child: new Container(
                        decoration: new ShapeDecoration(
                            shape: CircleBorder(side: BorderSide.none),
                            shadows: [
                              new BoxShadow(
                                  offset: new Offset(0.0, 1.0),
                                  color: Colors.black12,
                                  blurRadius: 10.0,
                                  spreadRadius: 5.0)
                            ]),
                        child: new CircleAvatar(
                          backgroundColor: enableStatus
                              ? BaseColors.secondaryDark
                              : Colors.white,
                          radius: 30.0,
                          child: new Icon(
                            enableStatus ? Icons.lock : Icons.lock_open,
                            color: enableStatus
                                ? Colors.white
                                : BaseColors.secondaryDark,
                          ),
                        ),
                      )),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  void saveEnableDisableSetting({bool value}) async {
    SharedPreferences pref = await SharedPreferences.getInstance();
    await pref.setBool(AppConfig.SILENCE_IS_ENABLE, value);
    PluginHandShake().toggleEnable();
  }
}

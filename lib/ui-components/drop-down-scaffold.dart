import 'package:flutter/material.dart';
import 'package:meta/meta.dart';

import '../util/menu-controller.dart';

class DropDownScaffold extends StatefulWidget {
  final Widget menu;
  final Widget frontContent;
  DropDownScaffold({@required this.menu, @required this.frontContent});

  @override
  _DropDownScaffoldState createState() => new _DropDownScaffoldState();
}

class _DropDownScaffoldState extends State<DropDownScaffold>
    with TickerProviderStateMixin {
  MenuController menuController;
  final key = <String, GlobalKey>{
    "scaffold_key": new GlobalKey<ScaffoldState>(),
  };

  @override
  void initState() {
    super.initState();
    menuController = new MenuController(vsync: this)
      ..addListener(() => setState(() => {}));
  }

  @override
  void dispose() {
    menuController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return new Stack(
      fit: StackFit.expand,
      children: <Widget>[
        widget.menu,
        dropFrontContent(widget.frontContent),
      ],
    );
  }

  Widget dropFrontContent(Widget content) {
    double dropValue = 295.0 * menuController.perOpen;
    double scaleValue = 1.0 - (0.05 * menuController.perOpen);
    double cornerValue = 10.0 * menuController.perOpen;
    return new GestureDetector(
      onVerticalDragEnd: (_) {
        // menuController.state == MenuController.CLOSE
        //     ? null
        //     : menuController.close();
      },
      child: new Transform(
          transform: new Matrix4.translationValues(0.0, dropValue, 0.0)
            ..scale(scaleValue, scaleValue),
          alignment: Alignment.bottomCenter,
          child: new Container(
            decoration: new BoxDecoration(boxShadow: [
              new BoxShadow(
                  color: Colors.black12,
                  offset: new Offset(0.0, -5.0),
                  blurRadius: 30.0,
                  spreadRadius: 25.0)
            ]),
            child: new ClipRRect(
              borderRadius: new BorderRadius.circular(cornerValue),
              child: content,
            ),
          )),
    );
  }
}

typedef Widget DropDownScaffoldBuilder(
    BuildContext cxt, MenuController mController, Map<String, GlobalKey> key);

class DropDownScaffoldProvider extends StatelessWidget {
  final DropDownScaffoldBuilder builder;
  DropDownScaffoldProvider({@required this.builder});

  @override
  Widget build(BuildContext context) {
    return builder(context, getMenuController(context), getKey(context));
  }

  MenuController getMenuController(BuildContext context) {
    final state =
        context.ancestorStateOfType(new TypeMatcher<_DropDownScaffoldState>())
            as _DropDownScaffoldState;
    return state.menuController;
  }

  Map<String, GlobalKey<State<StatefulWidget>>> getKey(BuildContext context) {
    final state =
        context.ancestorStateOfType(new TypeMatcher<_DropDownScaffoldState>())
            as _DropDownScaffoldState;
    return state.key;
  }
}

import 'package:flutter/material.dart';

import '../ui-components/content-screen.dart';
import '../ui-components/drop-down-scaffold.dart';
import '../ui-components/menu-screen.dart';

class HomePage extends StatefulWidget {
  @override
  _HomePageState createState() => new _HomePageState();
}

class _HomePageState extends State<HomePage> {

  @override
  Widget build(BuildContext context) {
    return new DropDownScaffold(
      menu: new MenuScreen(),
      frontContent: new FrontContent(),
    );
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';

import '../model/appmodel.dart';
import '../redux/state-manager.dart';
import 'content-screen.dart';
import 'list-item.dart';

class TimeList extends StatefulWidget {
  @override
  TimeListState createState() {
    return new TimeListState();
  }
}

class TimeListState extends State<TimeList> {
  @override
  Widget build(BuildContext context) {
    return new KeyProvider(
      builder: (cxt, keys) {
        return new StoreConnector<AppState, List<AppModel>>(
            converter: (store) => store.state.items,
            distinct: true,
            builder: (cxt, viewModel) {
              return new ListView.builder(
                itemCount: viewModel.length,
                itemBuilder: (cxt, cIndex) => new SingleListItem(
                      currIndex: cIndex,
                    ),
              );
            });
      },
    );
  }
}

class OopsListItem extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return new Theme(
      data: Theme
          .of(context)
          .copyWith(brightness: Brightness.light, cardColor: Colors.white),
      child: new Card(
        shape: new RoundedRectangleBorder(
            borderRadius: new BorderRadius.circular(20.0)),
        elevation: 6.0,
        margin: const EdgeInsets.symmetric(horizontal: 10.0),
        child: new Center(
          child: new Image.asset(
            "assets/image/oops.png",
            width: 100.0,
            height: 100.0,
          ),
        ),
      ),
    );
  }
}

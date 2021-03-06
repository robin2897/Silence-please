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
      builder: (context, keys) {
        return new StoreConnector<AppState, List<AppModel>>(
            converter: (store) => store.state.items,
            distinct: true,
            builder: (context, viewModel) {
              return new ListView.builder(
                itemCount: viewModel.length,
                itemBuilder: (context, cIndex) => new SingleListItem(
                      currIndex: cIndex,
                    ),
              );
            });
      },
    );
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:meta/meta.dart';

import '../model/appmodel.dart';
import '../redux/state-manager.dart';
import '../redux/view-model.dart';
import '../util/fonts-colors.dart';
import '../util/sperator.dart';
import 'content-screen.dart';

class SingleListItem extends StatefulWidget {
  final int currIndex;
  SingleListItem({@required this.currIndex});

  @override
  _SingleListItemState createState() => new _SingleListItemState();
}

class _SingleListItemState extends State<SingleListItem>
    with SingleTickerProviderStateMixin {
  @override
  Widget build(BuildContext context) {
    return new Theme(
      data: Theme
          .of(context)
          .copyWith(brightness: Brightness.light, cardColor: Colors.white),
      child: new StoreConnector<AppState, List<AppModel>>(
        converter: (store) => store.state.items,
        builder: (context, viewModel) {
          int currIndex = widget.currIndex;
          AppModel currModel = viewModel[currIndex];
          TimeExtractor startExc = timeExtractor(
              rawTime: new TimeOfDay.fromDateTime(currModel.startTime),
              context: context);
          TimeExtractor endExc = timeExtractor(
              rawTime: new TimeOfDay.fromDateTime(currModel.endTime),
              context: context);

          return new Card(
            shape: new RoundedRectangleBorder(
                borderRadius: new BorderRadius.circular(20.0)),
            elevation: 6.0,
            margin: const EdgeInsets.only(left: 10.0, right: 10.0, bottom: 8.0),
            child: new ExpansionTile(
              initiallyExpanded: currModel.isExpanded,
              onExpansionChanged: (value) => currModel.isExpanded = value,
              title: new Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8.0),
                child: new Row(
                  children: <Widget>[
                    new Expanded(
                      child: new Column(children: <Widget>[
                        startEndTimeLabel(
                            time: startExc.time, suffix: startExc.suffix),
                        startEndTimeLabel(
                            time: endExc.time, suffix: endExc.suffix)
                      ]),
                    ),
                    new StoreConnector<AppState, ViewModel>(
                      converter: (store) => new ViewModel.update(
                          updateAction: (model) =>
                              store.dispatch(UpdateAction(model: model))),
                      builder: (context, viewModel) {
                        return new Switch(
                          onChanged: (value) => setState(() {
                                currModel.isActive = value;
                                viewModel.updateAction(currModel);
                              }),
                          value: currModel.isActive,
                        );
                      },
                    )
                  ],
                ),
              ),
              children: <Widget>[
                new Padding(
                  padding: const EdgeInsets.symmetric(vertical: 8.0),
                ),
                new Divider(
                  height: 0.5,
                ),
                new Padding(
                  padding: const EdgeInsets.symmetric(vertical: 16.0),
                  child: new Material(
                    color: Colors.transparent,
                    child: new StoreConnector<AppState, ViewModel>(
                      converter: (store) => new ViewModel.update(
                          updateAction: (model) =>
                              store.dispatch(UpdateAction(model: model))),
                      builder: (context, viewModel) {
                        return new Row(
                          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                          children: <Widget>[
                            dayButton(
                                label: "M",
                                checked: currModel.days[0] == 1,
                                ontap: () {
                                  currModel.days[0] =
                                      currModel.days[0] == 1 ? 0 : 1;
                                  viewModel.updateAction(currModel);
                                }),
                            dayButton(
                                label: "T",
                                checked: currModel.days[1] == 1,
                                ontap: () {
                                  currModel.days[1] =
                                      currModel.days[1] == 1 ? 0 : 1;
                                  viewModel.updateAction(currModel);
                                }),
                            dayButton(
                                label: "W",
                                checked: currModel.days[2] == 1,
                                ontap: () {
                                  currModel.days[2] =
                                      currModel.days[2] == 1 ? 0 : 1;
                                  viewModel.updateAction(currModel);
                                }),
                            dayButton(
                                label: "T",
                                checked: currModel.days[3] == 1,
                                ontap: () {
                                  currModel.days[3] =
                                      currModel.days[3] == 1 ? 0 : 1;
                                  viewModel.updateAction(currModel);
                                }),
                            dayButton(
                                label: "F",
                                checked: currModel.days[4] == 1,
                                ontap: () {
                                  currModel.days[4] =
                                      currModel.days[4] == 1 ? 0 : 1;
                                  viewModel.updateAction(currModel);
                                }),
                            dayButton(
                                label: "S",
                                checked: currModel.days[5] == 1,
                                ontap: () {
                                  currModel.days[5] =
                                      currModel.days[5] == 1 ? 0 : 1;
                                  viewModel.updateAction(currModel);
                                }),
                            dayButton(
                                label: "S",
                                checked: currModel.days[6] == 1,
                                ontap: () {
                                  currModel.days[6] =
                                      currModel.days[6] == 1 ? 0 : 1;
                                  viewModel.updateAction(currModel);
                                }),
                          ],
                        );
                      },
                    ),
                  ),
                ),
                new Divider(
                  height: 0.5,
                ),
                bottomArea(currIndex: currIndex, currModel: currModel)
              ],
            ),
          );
        },
      ),
    );
  }

  Widget startEndTimeLabel({@required String time, @required String suffix}) {
    return new Row(
      crossAxisAlignment: CrossAxisAlignment.baseline,
      textBaseline: TextBaseline.alphabetic,
      children: <Widget>[
        new Text(
          time,
          style: Theme
              .of(context)
              .textTheme
              .body1
              .copyWith(fontSize: 56.0, color: Colors.black.withOpacity(0.7)),
        ),
        new Text(
          suffix,
          style: Theme
              .of(context)
              .textTheme
              .body1
              .copyWith(fontSize: 20.0, color: Colors.black.withOpacity(0.7)),
        ),
      ],
    );
  }

  Widget dayButton(
      {@required String label,
      @required bool checked,
      @required Function ontap}) {
    return new Container(
      width: 40.0,
      height: 40.0,
      decoration: new ShapeDecoration(
        shape: new RoundedRectangleBorder(
            borderRadius: new BorderRadius.circular(6.0)),
        color: checked
            ? BaseColors.secondaryLight.withOpacity(0.6)
            : Colors.transparent,
      ),
      child: new InkWell(
        onTap: ontap,
        child: new Center(
          child: new Text(
            label,
            style: Theme
                .of(context)
                .textTheme
                .body1
                .copyWith(fontSize: 20.0, color: Colors.black.withOpacity(0.8)),
          ),
        ),
      ),
    );
  }

  Widget bottomArea({@required AppModel currModel, @required int currIndex}) {
    return new Padding(
      padding: const EdgeInsets.all(10.0),
      child: new Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          new Expanded(
              child: new Align(
            alignment: Alignment.centerLeft,
            child: new KeyProvider(builder: (context, keys) {
              var scaffoldKey =
                  keys["scaffold_key"] as GlobalKey<ScaffoldState>;
              return new StoreConnector<AppState, ViewModel>(
                converter: (store) => new ViewModel.delete(
                    deleteAction: (model, index) => store
                        .dispatch(DeleteAction(index: index, model: model))),
                builder: (context, viewModel) {
                  return IconButton(
                    icon: new Icon(Icons.delete),
                    onPressed: () {
                      scaffoldKey.currentState.showSnackBar(new SnackBar(
                        content: new Text(
                          "Deleted",
                          style: Theme
                              .of(context)
                              .textTheme
                              .body1
                              .copyWith(fontSize: 16.0),
                        ),
                      ));
                      viewModel.deleteAction(currModel, currIndex);
                    },
                    color: BaseColors.secondary,
                    tooltip: "Delete",
                  );
                },
              );
            }),
          )),
          new Theme(
            data: new ThemeData.light(),
            child: new DropdownButtonHideUnderline(
              child: new StoreConnector<AppState, ViewModel>(
                converter: (store) => new ViewModel.update(
                    updateAction: (model) =>
                        store.dispatch(UpdateAction(model: model))),
                builder: (context, viewModel) {
                  return new DropdownButton(
                    items: [
                      dropDownMenuItem(
                          label: "Silent", icon: Icons.volume_off, value: "S"),
                      dropDownMenuItem(
                          label: "Vibrate", icon: Icons.vibration, value: "V"),
                    ],
                    onChanged: (value) {
                      if (value == "S") {
                        currModel.isSilent = true;
                        currModel.isVibrate = false;
                        viewModel.updateAction(currModel);
                      } else if (value == "V") {
                        currModel.isSilent = false;
                        currModel.isVibrate = true;
                        viewModel.updateAction(currModel);
                      }
                    },
                    value: currModel.isSilent ? "S" : "V",
                  );
                },
              ),
            ),
          )
        ],
      ),
    );
  }

  DropdownMenuItem<String> dropDownMenuItem(
      {@required String label,
      @required IconData icon,
      @required String value}) {
    return new DropdownMenuItem(
      child: new Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          new Padding(
            padding: const EdgeInsets.all(10.0),
            child: new Icon(icon),
          ),
          new Padding(
            padding: const EdgeInsets.all(10.0),
            child: new Center(
              child: new Text(
                label,
                style: Theme.of(context).textTheme.button.copyWith(
                    color: Colors.black.withOpacity(0.7), fontSize: 16.0),
              ),
            ),
          ),
        ],
      ),
      value: value,
    );
  }
}

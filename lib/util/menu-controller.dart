import 'package:flutter/material.dart';
import 'package:meta/meta.dart';

class MenuController extends ChangeNotifier {
  static const int OPEN = 2;
  static const int OPENING = 1;
  static const int CLOSE = -1;
  static const int CLOSING = 0;

  int state = CLOSE;
  final TickerProvider vsync;
  final AnimationController _controller;

  MenuController({@required this.vsync})
      : _controller = AnimationController(vsync: vsync) {
    _controller
      ..duration = Duration(milliseconds: 150)
      ..addListener(() {
        notifyListeners();
      })
      ..addStatusListener((status) {
        switch (status) {
          case AnimationStatus.forward:
            state = OPENING;
            break;
          case AnimationStatus.reverse:
            state = CLOSING;
            break;
          case AnimationStatus.completed:
            state = OPEN;
            break;
          case AnimationStatus.dismissed:
            state = CLOSE;
            break;
        }
        notifyListeners();
      });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  get perOpen {
    return _controller.value;
  }

  void open() {
    _controller.forward();
  }

  void close() {
    _controller.reverse();
  }

  void toggle() {
    if (state == OPEN)
      close();
    else if (state == CLOSE) open();
  }
}

import 'package:flutter/material.dart';
import 'package:silence_please/application.dart';
import 'package:silence_please/config/config.dart';

void main() {   
  AppConfig.flavor = Flavor.PAID;
  runApp(new Application());
}


#!/bin/bash

ps x | grep jstatd | grep -v grep | awk '{print $1}' | xargs kill -15
ps x | grep web-client | grep -v grep | awk '{print $1}' | xargs kill -15
ps x | grep ServerMain | grep -v grep | awk '{print $1}' | xargs kill -15

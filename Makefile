
.DEFAULT_GOAL := help

GRADLE := ./gradlew
ADB ?= adb
APP_ID := com.portofino.launcher
ACTIVITY := $(APP_ID)/.MainActivity

.PHONY: help build install run deploy restart uninstall clean \
        rebuild devices logs crash test release

help:
	@grep -E '^[a-zA-Z_-]+:.*##' $(MAKEFILE_LIST) | \
	awk 'BEGIN {FS = ":.*## "} {printf "  make %-12s %s\n", $$1, $$2}'

build:
	$(GRADLE) assembleDebug

install:
	$(GRADLE) installDebug

run:
	$(ADB) shell am start -n $(ACTIVITY)

deploy: install run

restart:
	$(ADB) shell am force-stop $(APP_ID)
	$(ADB) shell am start -n $(ACTIVITY)

uninstall:
	$(ADB) uninstall $(APP_ID)

clean: ##
	$(GRADLE) clean

rebuild: clean build

devices:
	$(ADB) devices -l

logs:
	$(ADB) logcat -v time -s AndroidRuntime:E

crash:
	$(ADB) logcat -b crash -d

test:
	$(GRADLE) testDebugUnitTest

release:
	$(GRADLE) installRelease

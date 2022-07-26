# Media Feed Player - Android

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.7] - 2021-01-01
### [Changes]
- Update third party SDK version

### [Added]
- Prepare AdMob open bidding
- FAN SDK in preparation for open bidding and mediation

## [1.1.6] - 2020-07-31
### [Changes]
- Move interstitial to player activity to allow video load while interstitial is in foreground
- Break out favourite into its own separate ad unit
- Add explicit reference to constraint layout instead of relying on Kotlin Extension

## [1.1.5] - 2020-07-26
### [Added]
- Firebase crashlytic tracking

## [1.1.4] - 2020-07-19
- Added Firebase Tracking in channel user guide

## [1.1.3] - 2020-07-19
- User Guide button in empty Channels fragment

## [1.1.2] - 2020-07-18
### [Fixed]
- Fix bug when a new interstitial ad is not load when user finish a video
- Handle deep link when open from Facebook

## [1.1.1] - 2020-07-17
### Changes
- Enable banner ads
- Limit banner when navigating back stack

## [1.1] - 2020-07-16
### Added
- Favourite Videos

## [1.0.2] - 2020-07-15
### Fixed
- Dynamic link handler called from main activity instead of new channel activity to keep user in the app

## [1.0.1] - 2020-07-13
### Added
- Added banner ads and intersitial ads

## [1.0] - 2020-07-12
### Added
- Initial release
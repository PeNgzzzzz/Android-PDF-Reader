# Android PDF Reader

## Introduction
This project is an Android-based PDF reader that allows users to read, annotate, and navigate through PDF documents. The application is designed to work on Android tablets and supports a range of features including drawing, highlighting, erasing, zooming, and panning.

## Features

### Initial Launch
- **Sample PDF**: On first launch, a sample PDF is loaded into the application.
- **Titlebar**: Displays the name of the currently loaded PDF.
- **Statusbar**: Shows the current page number and total number of pages (e.g., "page 2/5").

### Navigation
- **Page Browsing**: Users can browse forward and backward through the document pages.
- **Status Update**: The status bar updates to indicate the current page.
- **Swipe/Buttons**: Users can navigate pages either by swiping left/right or using back/forward buttons.

### Annotation
- **Drawing**: Users can draw on the current page with a default color and line thickness.
- **Highlighting**: A thick, transparent yellow brush allows users to highlight text without obscuring it.
- **Erasing**: Users can erase any existing drawing or highlighting.

### Zoom & Pan
- **Two-Finger Gestures**: Users can zoom in and out using a two-finger pinch gesture.
- **Panning**: When zoomed in, users can pan around the document.
- **Direct Manipulation**: All gestures adhere to direct manipulation principles.

### Undo/Redo
- **Undo**: Users can undo the last 5 actions.
- **Redo**: Users can redo actions that were undone.

### Additional Features
- **Orientation Handling**: The application supports both portrait and landscape modes without data loss.
- **Multi-Page Support**: Supports multi-page PDFs and retains annotations when navigating between pages.
- **Data Persistence**: Annotations are saved when the user changes pages or exits the application.

### Gesture Flexibility (NEW)
- **Two-Finger Zooming**: Mandatory two-finger zooming is implemented.
- **Modal Operations**: Users can toggle between draw and pan modes, using one finger for both.

## Technical Requirements
- **Android API**: Must be at least API 30.
- **AVD**: Pixel C tablet AVD with API 30 or later.
- **Orientation**: Must handle orientation changes without data loss.

## How to Run
1. Clone the repository.
2. Open the project in your Android development environment.
3. Run the application on the Pixel C tablet AVD.

# intelij-plugin-macro-live-template

Plugin add macro in Intelij to find line from Makefile.

### Usage

In main project directory should be file "Makefile".
In this file below first 100 lines must be line starting with "PLUGINNAME =".
Rest of this line is used as plugin name.

### Add live template
- File -> Settings -> Editor -> Live Templates
- On right side click "+" and add Live Template.
- Set abbreviation, define context,
- Add Template text (for example: $var$)
- Click on Edit variables
- Select variable row
- Open combobox Expression
- Choose "pluginName()"
- Confirm all windows

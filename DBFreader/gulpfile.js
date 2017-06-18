var gulp              = require('gulp');
var electronInstaller = require('electron-winstaller');
 
gulp.task('build-installer', function(done)
{
    resultPromise = electronInstaller.createWindowsInstaller({
        appDirectory    : '../../',
        outputDirectory : '../../build',
        exe             : 'DBFReader.exe',
        setupIcon       : './img/appicon.ico'
    });

    return resultPromise;
});

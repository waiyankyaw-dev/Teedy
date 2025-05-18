'use strict';

/**
 * Document view content controller.
 */
angular.module('docs').controller('DocumentViewContent', function ($scope, $rootScope, $stateParams, Restangular, $dialog, $state, Upload, $translate, $uibModal) {
  $scope.displayMode = _.isUndefined(localStorage.fileDisplayMode) ? 'grid' : localStorage.fileDisplayMode;
  $scope.openedFile = undefined;

  /**
   * Watch for display mode change.
   */
  $scope.$watch('displayMode', function (next) {
    localStorage.fileDisplayMode = next;
  });

  /**
   * Configuration for file sorting.
   */
  $scope.fileSortableOptions = {
    forceHelperSize: true,
    forcePlaceholderSize: true,
    tolerance: 'pointer',
    start: function() {
      $(this).addClass('currently-dragging');
    },
    stop: function () {
      var _this = this;
      setTimeout(function(){
        $(_this).removeClass('currently-dragging');
      }, 300);

      // Send new positions to server
      $scope.$apply(function () {
        Restangular.one('file').post('reorder', {
          id: $stateParams.id,
          order: _.pluck($scope.files, 'id')
        });
      });
    }
  };

  /**
   * Load files from server.
   */
  $scope.loadFiles = function () {
    Restangular.one('file/list').get({ id: $stateParams.id }).then(function (data) {
      $scope.files = data.files;
    });
  };
  $scope.loadFiles();

  /**
   * Navigate to the selected file.
   */
  $scope.openFile = function (file, $event) {
    if ($($event.target).parents('.currently-dragging').length === 0) {
      $scope.openedFile = file;
      $state.go('document.view.content.file', { id: $stateParams.id, fileId: file.id });
    }
  };

  /**
   * Delete a file.
   */
  $scope.deleteFile = function (file) {
    var title = $translate.instant('document.view.content.delete_file_title');
    var msg = $translate.instant('document.view.content.delete_file_message');
    var btns = [
      {result: 'cancel', label: $translate.instant('cancel')},
      {result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}
    ];

    $dialog.messageBox(title, msg, btns, function (result) {
      if (result === 'ok') {
        Restangular.one('file', file.id).remove().then(function () {
          // File deleted, decrease used quota
          $rootScope.userInfo.storage_current -= file.size;

          // Update local data
          $scope.loadFiles();
        });
      }
    });
  };

  /**
   * Upload a new version.
   */
  $scope.uploadNewVersion = function (files, file) {
    if (!$scope.document.writable || !files || files.length === 0) {
      return;
    }

    var uploadedfile = files[0];
    var previousFileId = file.id;
    file.id = undefined;
    file.progress = 0;
    file.name = uploadedfile.name;
    file.create_date = new Date().getTime();
    file.mimetype = uploadedfile.type;
    file.version++;
    $scope.uploadFile(uploadedfile, file, previousFileId);
  };

  /**
   * File has been drag & dropped.
   */
  $scope.fileDropped = function (files) {
    if (!$scope.document.writable) {
      return;
    }

    if (files && files.length) {
      // Sort by filename
      files = _.sortBy(files, 'name');

      // Adding files to the UI
      var newfiles = [];
      _.each(files, function (file) {
        var newfile = {
          progress: 0,
          name: file.name,
          create_date: new Date().getTime(),
          mimetype: file.type,
          status: $translate.instant('document.view.content.upload_pending')
        };
        $scope.files.push(newfile);
        newfiles.push(newfile);
      });

      // Uploading files sequentially
      var key = 0;
      var then = function() {
        if (files[key]) {
          $scope.uploadFile(files[key], newfiles[key++]).then(then);
        }
      };
      then();
    }
  };

  /**
   * Upload a file.
   */
  $scope.uploadFile = function(file, newfile, previousFileId) {
    // Upload the file
    newfile.status = $translate.instant('document.view.content.upload_progress');
    return Upload.upload({
      method: 'PUT',
      url: '../api/file',
      file: file,
      fields: {
        id: $stateParams.id,
        previousFileId: previousFileId
      }
    })
    .progress(function(e) {
      newfile.progress = parseInt(100.0 * e.loaded / e.total);
    })
    .success(function(data) {
      // Update local model with real data
      newfile.id = data.id;
      newfile.size = data.size;

      // New file uploaded, increase used quota
      $rootScope.userInfo.storage_current += data.size;
    })
    .error(function (data) {
      newfile.status = $translate.instant('document.view.content.upload_error');
      if (data.type === 'QuotaReached') {
        newfile.status += ' - ' + $translate.instant('document.view.content.upload_error_quota');
      }
    });
  };

  /**
   * Rename a file.
   */
  $scope.renameFile = function (file) {
    $uibModal.open({
      templateUrl: 'partial/docs/file.rename.html',
      controller: 'FileRename',
      resolve: {
        file: function () {
          return angular.copy(file);
        }
      }
    }).result.then(function (fileUpdated) {
      if (fileUpdated === null) {
        return;
      }

      // Rename the file
      Restangular.one('file/' + file.id).post('', {
        name: fileUpdated.name
      }).then(function () {
        file.name = fileUpdated.name;
      });
    });
  };

  /**
   * Process a file.
   */
  $scope.processFile = function (file) {
    Restangular.one('file/' + file.id).post('process').then(function () {
      file.processing = true;
    });
  };

  /**
   * Open versions history.
   */
  $scope.openVersions = function (file) {
    $uibModal.open({
      templateUrl: 'partial/docs/file.versions.html',
      controller: 'ModalFileVersions',
      size: 'lg',
      resolve: {
        file: function () {
          return file;
        }
      }
    });
  };

  /**
   * Translate a file.
   */
  $scope.translateFile = function (file) {
    $uibModal.open({
      template: `
        <div class="modal-header">
          <h3 class="modal-title">Translate Documenttt</h3>
        </div>
        <div class="modal-body">
          <select ng-model="targetLanguage" class="form-control">
            <option value="es">Spanish</option>
            <option value="fr">French</option>
            <option value="de">German</option>
            <option value="de">English</option>
            <option value="zh">Chinese</option>
            <option value="ja">Japanese</option>
          </select>
          <div ng-show="translatedText" class="mt-3">
            <h4>Translated Text:</h4>
            <pre>{{translatedText}}</pre>
          </div>
          <div ng-show="error" class="mt-3 alert alert-danger">
            {{error}}
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" ng-click="translate()">Translate</button>
          <button class="btn btn-default" ng-click="$dismiss()">Close</button>
        </div>
      `,
      controller: function($scope, $uibModalInstance) {
        $scope.targetLanguage = 'es';
        $scope.translatedText = '';
        $scope.error = '';

        $scope.translate = function() {
          $scope.error = '';
          $scope.translatedText = '';
          Restangular.one('file', file.id).post('translate', {
            targetLanguage: $scope.targetLanguage
          }).then(function(response) {
            // Check if the response contains the rate limit error
            if (response.translated_text && response.translated_text.toLowerCase().includes('user rate limit exceeded')) {
//              $scope.translatedText = '我很帅 我很帅 我很帅 我很帅 我很帅 我很帅 我很帅 我很帅 我很帅 我很帅';
              $scope.translatedText = response.translated_text;
            } else {
              $scope.translatedText = response.translated_text;
            }
          }).catch(function(response) {
            // Handle any other errors
            $scope.error = 'Translation error: ' + (response.data && response.data.translated_text ? response.data.translated_text : 'Unknown error');
          });
        };
      }
    });
  };
});
'use strict';

/**
 * Settings user request management page controller.
 */
angular.module('docs').controller('SettingsUserRequest', function($scope, $rootScope, $dialog, Restangular, $translate) {
  // Default date format
  $scope.dateFormat = 'yyyy-MM-dd';

  /**
   * Load user requests from server.
   */
  $scope.loadRequests = function() {
    Restangular.one('user/request/list').get().then(function(data) {
      $scope.requests = data.requests;
    });
  };

  $scope.loadRequests();

  /**
   * Approve a user request.
   */
  $scope.approve = function(request) {
    var title = $translate.instant('settings.userrequest.approve_title');
    var msg = $translate.instant('settings.userrequest.approve_message', { username: request.username });
    var btns = [
      { result:'cancel', label: $translate.instant('cancel') },
      { result:'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }
    ];

    $dialog.messageBox(title, msg, btns, function(result) {
      if (result === 'ok') {
        Restangular.one('user/request/approve/' + request.id).post('').then(function() {
          $scope.loadRequests();
        });
      }
    });
  };

  /**
   * Reject a user request.
   */
  $scope.reject = function(request) {
    var title = $translate.instant('settings.userrequest.reject_title');
    var msg = $translate.instant('settings.userrequest.reject_message', { username: request.username });
    var btns = [
      { result:'cancel', label: $translate.instant('cancel') },
      { result:'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }
    ];

    $dialog.messageBox(title, msg, btns, function(result) {
      if (result === 'ok') {
        Restangular.one('user/request/reject/' + request.id).post('').then(function() {
          $scope.loadRequests();
        });
      }
    });
  };
});
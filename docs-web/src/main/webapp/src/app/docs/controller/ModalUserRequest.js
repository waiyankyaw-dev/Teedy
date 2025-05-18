'use strict';

/**
 * User request modal controller.
 */
angular.module('docs').controller('ModalUserRequest', function($scope, $uibModalInstance, Restangular, $dialog, $translate) {
  $scope.user = {};

  /**
   * Cancel the user request.
   */
  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };

  /**
   * Submit the user request.
   */
  $scope.submitRequest = function() {
    // Reset validation state
    if ($scope.userRequestForm.username.$error.already_used) {
      $scope.userRequestForm.username.$setValidity('already_used', true);
    }

    Restangular.one('user/request').put($scope.user).then(function() {
      $uibModalInstance.close();
      var title = $translate.instant('userrequest.sent_title');
      var msg = $translate.instant('userrequest.sent_message');
      var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
      $dialog.messageBox(title, msg, btns);
    }, function(e) {
      if (e.data.type === 'AlreadyExistingUsername') {
        $scope.userRequestForm.username.$setValidity('already_used', false);
      }
    });
  };
});
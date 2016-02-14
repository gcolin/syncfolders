angular.module('app', []).controller('Controller', ['$scope', function($scope) {
    $scope.clength = true;
    $scope.ctime = true;
    $scope.ccontent = false;
    $scope.chash = true;
    $scope.list = [];
    $scope.dup = [];
    $scope.pvalue = -1;

    $scope.start = function() {
        $scope.state = "run";
        window.controller.synchro($scope.source, $scope.dest, $scope.clength, $scope.ctime, $scope.ccontent);
    }
    $scope.startSim = function() {
        $scope.state = "run";
        window.controller.synchroSim($scope.source, $scope.dest, $scope.clength, $scope.ctime, $scope.ccontent);
    }
    $scope.startDup = function() {
        $scope.pvalue = -1;
        $scope.state = "run";
        window.controller.duplicate($scope.source, $scope.clength, $scope.chash);
    }
    
    $scope.removeSameFolder = function() {
        $scope.dup=[];
        $scope.pvalue = -1;
        $scope.state = "run";
        window.controller.removeSameFolder($scope.source, $scope.clength, $scope.chash);
    }

    window.bus = {
        message : function(m) {
            $scope.$evalAsync(function() {
                $scope.message = m;
            });
        },
        end : function() {
            $scope.$evalAsync(function() {
                $scope.state = "end";
            });
        },
        error : function(e) {
            $scope.$evalAsync(function() {
                $scope.error = e;
            });
        },
        list : function(v) {
            $scope.$evalAsync(function() {
                $scope.list.push(v);
            });
        },
        dup : function(v) {
            $scope.$evalAsync(function() {
                $scope.dup.push(v);
            });
        },
        percent : function(v) {
            $scope.$evalAsync(function() {
                $scope.pvalue = v;
            });
        }
    };
}]);
import Foundation

import FirebaseAuthUI
import FirebaseGoogleAuthUI
import MultiPlatformLibrary
import mokoMvvmFlowSwiftUI
import Combine

class SignInService: NSObject, ObservableObject, FUIAuthDelegate {
    let authUI: FUIAuth? = FUIAuth.defaultAuthUI()
    let userRepo = KoinHelper().getUserRepo()
        
    @Published var isSignedIn: KotlinBoolean = false
    
    private var isSignedInCancellable: AnyCancellable?

    
    override init() {
        super.init()
        
        authUI?.delegate = self
        
        let providers: [FUIAuthProvider] = [
            FUIGoogleAuth(authUI: authUI!)
        ]
        
        authUI?.providers = providers
        
        isSignedInCancellable = createPublisher(userRepo.isUserSignedIn)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { completion in
                debugPrint(completion)
            }, receiveValue: { [weak self] value in
                self?.isSignedIn = value
                print("signinservice: " + value.boolValue.description)
            })
    }

    
    func authUI(_ authUI: FUIAuth, didSignInWith user: User?, error: Error?) {
        print("user: \(user?.email ?? "none")")
        
        switch error {
            case .some(let error as NSError) where UInt(error.code) == FUIAuthErrorCode.userCancelledSignIn.rawValue:
              print("User cancelled sign-in")
            case .some(let error as NSError) where error.userInfo[NSUnderlyingErrorKey] != nil:
              print("Login error: \(error.userInfo[NSUnderlyingErrorKey]!)")
            case .some(let error):
              print("Login error: \(error.localizedDescription)")
            case .none:
              return
            }

    }
    

    func signOut() throws {
        try authUI?.signOut()
    }
}


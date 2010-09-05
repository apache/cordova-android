# this is for implementors mostly
class Test
  def initialize
    `git clone git@github.com:phonegap/mobile-spec.git && cd mobile-spec && droidgap create && cd ../mobilespec_android && ant debug install`
  end
end